package software.amazon.entityresolution.matchingworkflow;

import software.amazon.awssdk.services.entityresolution.EntityResolutionClient;
import software.amazon.cloudformation.LambdaWrapper;

public class ClientBuilder {

  public static EntityResolutionClient getClient() {
    return EntityResolutionClient.builder()
                                 .httpClient(LambdaWrapper.HTTP_CLIENT)
                                 .build();
  }
}
