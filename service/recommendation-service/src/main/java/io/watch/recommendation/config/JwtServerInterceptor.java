package io.watch.recommendation.config;

import io.grpc.*;
import io.watch.grpc.auth.AuthServiceGrpc;
import io.watch.grpc.auth.ValidateTokenRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RequiredArgsConstructor
public class JwtServerInterceptor implements ServerInterceptor {

    private final AuthServiceGrpc.AuthServiceBlockingStub authServiceStub;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String token = headers.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER));
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                var response = authServiceStub.validateToken(ValidateTokenRequest.newBuilder()
                        .setToken(token)
                        .build());
                if (response.getValid()) {
                    Context context = Context.current()
                            .withValue(Context.key("userId"), response.getUserId())
                            .withValue(Context.key("methodName"), call.getMethodDescriptor().getFullMethodName());
                    return Contexts.interceptCall(context, call, headers, next);
                }
            } catch (Exception e) {
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid token"), headers);
                return new ServerCall.Listener<ReqT>() {};
            }
        }
        call.close(Status.UNAUTHENTICATED.withDescription("Missing or invalid token"), headers);
        return new ServerCall.Listener<ReqT>() {};
    }
}
