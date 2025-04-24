package io.watch.recommendation.grpc;

import io.watch.grpc.auth.AuthServiceGrpc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {
}
