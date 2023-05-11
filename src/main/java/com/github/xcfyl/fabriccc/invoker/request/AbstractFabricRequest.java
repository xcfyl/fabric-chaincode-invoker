package com.github.xcfyl.fabriccc.invoker.request;

import com.github.xcfyl.fabriccc.invoker.handler.impl.*;
import com.github.xcfyl.fabriccc.invoker.utils.CommonUtils;
import com.github.xcfyl.fabriccc.invoker.context.FabricContext;
import com.github.xcfyl.fabriccc.invoker.handler.TypeParseHandler;
import org.apache.http.impl.execchain.RequestAbortedException;
import org.hyperledger.fabric.sdk.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 西城风雨楼
 */
public abstract class AbstractFabricRequest<T> implements IFabricRequest<T> {
    protected final ResultHandler<T> resultHandler;

    protected final Channel channel;

    protected final FabricContext fabricContext;

    protected final HFClient hfClient;

    protected final Class<?> resultClazz;

    protected final Class<?> genericClass;

    protected final long timeout;

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

    public AbstractFabricRequest(User user, FabricContext fabricContext, String channelName,
                                 Class<T> resultClazz,Class<?> genericClass, ResultHandler<T> resultHandler, long timeout) {
        channel = fabricContext.getChannel(channelName, user);
        this.fabricContext = fabricContext;
        this.hfClient = CommonUtils.getHfClient(user);
        this.resultClazz = resultClazz;
        this.resultHandler = resultHandler;
        this.timeout = timeout;
        this.genericClass = genericClass;
    }

    @SuppressWarnings("unchecked")
    protected T parseResult(byte[] data) {
        TypeParseHandler<?> parser = RESULT_PARSE_HANDLER_MAP.getOrDefault(resultClazz,
                new JsonParseHandler<>((Class<T>) resultClazz, genericClass));
        return (T) parser.parse(data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T send() {
        try {
            Collection<ProposalResponse> responses = getResponses();
            if (!CommonUtils.allResponsesIsOk(responses)) {
                // 如果发生了错误
                if (this instanceof InstallRequest) {
                    // 如果当前是一个安装请求，该请求是同步的请求
                    // 默认情况下，返回Boolean
                    channel.shutdown(false);
                    return (T) Boolean.valueOf("false");
                }
                // 否则的话，判断是否是Query请求
                if (this instanceof QueryRequest) {
                    // 如果是Query请求，那么该请求同样是一个同步请求，返回null
                    // 表示请求结果出错
                    channel.shutdown(false);
                    return null;
                }

                // 否则就是两个异步的请求，这个时候，通过ResultHandler传递结果回去
                if (resultHandler != null) {
                    channel.shutdown(false);
                    resultHandler.handleFailure(new RequestAbortedException("peer返回的状态码不全是200"));
                    return null;
                }
            }

            // 走到这里说明至少所有的peer返回的数据是一样的
            if (this instanceof InstallRequest) {
                // 如果当前是Install请求，那么走到这里说明已经安装成功了，那么直接返回true
                channel.shutdown(false);
                return (T) Boolean.valueOf("true");
            }

            // 否则就是下面的三个请求，下面的三个请求一定是有响应结果的
            byte[] data = CommonUtils.parseResponse(responses);
            // 解析结果
            T result = parseResult(data);
            if (this instanceof InvokeRequest || this instanceof InitRequest) {
                // 说明这两个需要异步处理结果
                channel.sendTransaction(responses).thenAccept(transactionEvent -> {
                    if (resultHandler != null) {
                        resultHandler.handleSuccess(result);
                    }
                    channel.shutdown(false);
                }).exceptionally(throwable -> {
                    if (resultHandler != null) {
                        resultHandler.handleFailure(throwable);
                    }
                    channel.shutdown(false);
                    return null;
                });
                return null;
            }

            // 走到这里说明只可能是query请求了，同步请求，直接返回结果
            channel.shutdown(false);
            return result;
        } catch (Exception e) {
            // 走到这里需要判断当前是什么类型的请求
            if (this instanceof InvokeRequest || this instanceof InitRequest) {
                if (resultHandler != null) {
                    resultHandler.handleFailure(e);
                }
            }
            channel.shutdown(false);
        }
        return null;
    }

    protected abstract Collection<ProposalResponse> getResponses() throws Exception;
}
