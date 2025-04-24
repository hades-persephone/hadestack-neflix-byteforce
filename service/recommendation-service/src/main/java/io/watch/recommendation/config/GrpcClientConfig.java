package io.watch.recommendation.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.watch.grpc.auth.AuthServiceGrpc;
import io.watch.grpc.movie.MovieServiceGrpc;
import io.watch.grpc.user.UserServiceGrpc;
import io.watch.recommendation.grpc.AuthServiceImpl;
import io.watch.recommendation.grpc.MovieServiceImpl;
import io.watch.recommendation.grpc.UserServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public Server grpcServer(MovieServiceImpl movieService, AuthServiceImpl authService, UserServiceImpl userService) {
        return ServerBuilder.forPort(9094)
                .addService(movieService)
                .addService(authService)
                .addService(userService)
                .build();
    }

    @Bean
    public ManagedChannel sharedChannel() {
        return ManagedChannelBuilder.forAddress("localhost", 9094)
                .usePlaintext()
                .build();
    }

    @Bean
    public UserServiceGrpc.UserServiceBlockingStub userServiceStub(ManagedChannel sharedChannel) {
        return UserServiceGrpc.newBlockingStub(sharedChannel);
    }

    @Bean
    public MovieServiceGrpc.MovieServiceBlockingStub movieServiceStub(ManagedChannel sharedChannel) {
        return MovieServiceGrpc.newBlockingStub(sharedChannel);
    }

    @Bean
    public AuthServiceGrpc.AuthServiceBlockingStub authServiceStub(ManagedChannel sharedChannel) {
        return AuthServiceGrpc.newBlockingStub(sharedChannel);
    }
}