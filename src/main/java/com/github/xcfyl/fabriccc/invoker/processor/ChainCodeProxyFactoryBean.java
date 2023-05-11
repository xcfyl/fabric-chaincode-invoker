package com.github.xcfyl.fabriccc.invoker.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xcfyl.fabriccc.invoker.annotation.*;
import com.github.xcfyl.fabriccc.invoker.context.FabricContext;
import com.github.xcfyl.fabriccc.invoker.request.ResultHandler;
import com.github.xcfyl.fabriccc.invoker.handler.TypeParseHandler;
import com.github.xcfyl.fabriccc.invoker.handler.impl.*;
import com.github.xcfyl.fabriccc.invoker.request.InitRequest;
import com.github.xcfyl.fabriccc.invoker.request.InstallRequest;
import com.github.xcfyl.fabriccc.invoker.request.InvokeRequest;
import com.github.xcfyl.fabriccc.invoker.request.QueryRequest;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.sdk.User;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.FactoryBean;

import javax.annotation.Resource;
import java.lang.reflect.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 西城风雨楼
 */
@Slf4j
public class ChainCodeProxyFactoryBean<T> implements FactoryBean<T> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private FabricContext fabricContext;

    private final Class<?> targetClass;

    @SuppressWarnings("rawuse")
    private final static Map<Class<?>, TypeParseHandler<?>> RESULT_PARSE_HANDLER_MAP = new HashMap<>();

    // 加入预置的类型解析器
    static {
        RESULT_PARSE_HANDLER_MAP.put(Long.class, new LongParseHandler());
        RESULT_PARSE_HANDLER_MAP.put(Double.class, new DoubleParseHandler());
        RESULT_PARSE_HANDLER_MAP.put(Float.class, new FloatParseHandler());
        RESULT_PARSE_HANDLER_MAP.put(Integer.class, new IntegerParseHandler());
        RESULT_PARSE_HANDLER_MAP.put(Boolean.class, new BooleanParseHandler());
        RESULT_PARSE_HANDLER_MAP.put(String.class, new StringParseHandler());

        RESULT_PARSE_HANDLER_MAP.put(int.class, new IntegerParseHandler());
        RESULT_PARSE_HANDLER_MAP.put(long.class, new LongParseHandler());
        RESULT_PARSE_HANDLER_MAP.put(double.class, new DoubleParseHandler());
        RESULT_PARSE_HANDLER_MAP.put(float.class, new FloatParseHandler());
        RESULT_PARSE_HANDLER_MAP.put(boolean.class, new BooleanParseHandler());
    }

    public ChainCodeProxyFactoryBean(Class<?> targetClass) {
        this.targetClass = targetClass;
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    @SuppressWarnings({"rawuse", "unchecked"})
    private String[] parseArgs(Object[] args, Class<?> genericClass) {
        int start = 1;
        if (args[1] instanceof ResultHandler) {
            start++;
        }
        String[] chainCodeArgs = new String[args.length - start];

        for (int i = 0; start < args.length; start++, i++) {
            // 怎么解决参数是数组类型啊
            TypeParseHandler<Object> typeParseHandler = (TypeParseHandler<Object>) RESULT_PARSE_HANDLER_MAP
                    .getOrDefault(args[start].getClass(), new JsonListAndBeanParseHandler<>(args[start].getClass(), genericClass));
            chainCodeArgs[i] = typeParseHandler.get(args[start]);
        }
        return chainCodeArgs;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public T getObject() throws Exception {
        // 这里要进行相应的动态代理
        return (T) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class<?>[]{targetClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 解析该方法的信息
                if (!check(targetClass)) {
                    throw new BeanCreationException("被ChainCodeProxy标注的类中的方法注解标注不合法");
                }

                if (method.isAnnotationPresent(Invoke.class) || method.isAnnotationPresent(Query.class)) {
                    if (checkMethod(method)) {
                        throw new BeanCreationException("Invoke和Query调用method的第一个参数必须是User对象");
                    }
                }

                // 获取resultType和returnType
                Class<?> returnType = method.getReturnType();
                Class<?> resultType = null;
                Type genericReturnType = method.getGenericReturnType();
                if (genericReturnType instanceof ParameterizedType) {
                    ParameterizedType type = (ParameterizedType) genericReturnType;
                    Type argument = type.getActualTypeArguments()[0];
                    if (argument instanceof Class) {
                        resultType = (Class<?>) argument;
                    }
                }

                if (method.isAnnotationPresent(Invoke.class)) {
                    // 说明当前方法执行的是invoke调用
                    log.debug("当前执行的是invoke调用");
                    User user = (User) args[0];
                    String funcName = method.getName();
                    Invoke invoke = method.getAnnotation(Invoke.class);
                    Class<?> genericClass = resultType == null ? invoke.genericClass() : resultType;
                    String[] chainCodeArgs = parseArgs(args, genericClass);
                    log.debug("invoke的参数为: " + objectMapper.writeValueAsString(chainCodeArgs));

                    // 这是在获取注解上面的ResultHandler声明
                    Class<? extends ResultHandler<?>> handlerClass;
                    ResultHandler resultHandler;
                    if (!(args[1] instanceof ResultHandler)) {
                        handlerClass = invoke.resultHandler();
                        resultHandler = handlerClass.getConstructor().newInstance();
                    } else {
                        // 如果发现用户在方法中设置ResultHandler，那么优先使用方法中的ResultHandler
                        resultHandler = (ResultHandler) args[1];
                        handlerClass = (Class<? extends ResultHandler<?>>) args[1].getClass();
                    }

                    Type[] types = handlerClass.getGenericInterfaces();
                    if (types.length != 1) {
                        throw new BeanCreationException("ResultHandler的参数错误");
                    }
                    Class resultClass = null;
                    if (types[0] instanceof ParameterizedType) {
                        ParameterizedType type = (ParameterizedType) types[0];
                        Type argument = type.getActualTypeArguments()[0];
                        if (argument instanceof Class) {
                            // 如果发现handler的泛型参数是一个Class
                            resultClass = (Class) type.getActualTypeArguments()[0];
                        } else if (argument instanceof ParameterizedType) {
                            // 如果发现handler的泛型参数还是一个泛型参数
                            ParameterizedType parameterizedType = (ParameterizedType) argument;
                            // 那么找到该泛型参数的实际类型
                            Type actualTypeArguments = parameterizedType.getActualTypeArguments()[0];
                            resultClass = (Class) parameterizedType.getRawType();
                            if (resultClass != List.class) {
                                throw new BeanCreationException("ResultHandler的泛型参数不是List<Bean>形式");
                            }
                            if (actualTypeArguments instanceof Class) {
                                // 这是泛型类型，走到这里说明这是一个List
                                genericClass = (Class) actualTypeArguments;
                            } else {
                                throw new BeanCreationException("resultHandler的泛型参数错误");
                            }
                        }
                    } else {
                        throw new BeanCreationException("Invoke调用失败");
                    }

                    long timeout = invoke.timeout();
                    // 创建Invoke请求
                    InvokeRequest invokeRequest = new InvokeRequest<>(fabricContext, user, resultClass,
                            genericClass, funcName, chainCodeArgs, resultHandler, timeout);
                    invokeRequest.send();
                } else if (method.isAnnotationPresent(Query.class)) {
                    // 说明当前方法执行的是query调用
                    log.debug("当前执行的是query调用");

                    User user = (User) args[0];
                    String funcName = method.getName();
                    Query query = method.getAnnotation(Query.class);
                    Class<?> genericClass = resultType == null ? query.genericClass() : resultType;
                    String[] chainCodeArgs = parseArgs(args, genericClass);
                    log.debug("query的参数是: {}", objectMapper.writeValueAsString(chainCodeArgs));
                    long timeout = query.timeout();
                    // 创建Invoke请求
                    QueryRequest queryRequest = new QueryRequest(user, fabricContext, returnType,
                            genericClass, funcName, chainCodeArgs, timeout);
                    return queryRequest.send();
                } else if (method.isAnnotationPresent(Init.class)) {
                    // 说明当前执行的是init调用
                    log.debug("当前执行的是init调用");
                    Init init = method.getAnnotation(Init.class);
                    Class<? extends ResultHandler<?>> handlerClass = init.resultHandler();
                    ResultHandler resultHandler = handlerClass.getConstructor().newInstance();
                    long timeout = init.timeout();
                    InitRequest request = new InitRequest(fabricContext, resultHandler, timeout);
                    return request.send();
                } else if (method.isAnnotationPresent(Install.class)) {
                    // 说明当前执行的是Install调用
                    log.debug("当前执行的是install调用");
                    Install install = method.getAnnotation(Install.class);
                    long timeout = install.timeout();
                    InstallRequest installRequest = new InstallRequest(fabricContext, timeout);
                    return installRequest.send();
                }
                return null;
            }
        });
    }

    private boolean checkMethod(Method method) {
        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) {
            return true;
        }
        return parameters[0].getType() != User.class;
    }

    private boolean check(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Init.class)
                    || method.isAnnotationPresent(Invoke.class)
                    || method.isAnnotationPresent(Query.class)
                    || method.isAnnotationPresent(Install.class)) {
                continue;
            }
            return false;
        }
        return true;
    }

    @Override
    public Class<?> getObjectType() {
        return targetClass;
    }
}
