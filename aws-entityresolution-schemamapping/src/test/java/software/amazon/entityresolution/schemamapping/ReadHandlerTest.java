package software.amazon.entityresolution.schemamapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.entityresolution.EntityResolutionClient;
import software.amazon.awssdk.services.entityresolution.model.AccessDeniedException;
import software.amazon.awssdk.services.entityresolution.model.GetSchemaMappingRequest;
import software.amazon.awssdk.services.entityresolution.model.GetSchemaMappingResponse;
import software.amazon.awssdk.services.entityresolution.model.InternalServerException;
import software.amazon.awssdk.services.entityresolution.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.entityresolution.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.entityresolution.model.ResourceNotFoundException;
import software.amazon.awssdk.services.entityresolution.model.SchemaInputAttribute;
import software.amazon.awssdk.services.entityresolution.model.ThrottlingException;
import software.amazon.awssdk.services.entityresolution.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {

    private static final Instant TIME = Instant.now();
    private static final String SCHEMA_NAME = "schemaName";
    private static final String DESCRIPTION = "description";
    private static final String FIELD_NAME = "fieldName";
    private static final String GROUP_NAME = "groupName";
    private static final String MATCH_KEY = "matchKey";
    private static final String TYPE = "type";
    private static final boolean HAS_WORKFLOWS = false;
    private static final String SCHEMA_ARN = "arn:aws:entityresolution:us-east-1:123456789012:schemamapping"
        + "/schemaName";
    private static final Map<String, String> DESIRED_TAGS = ImmutableMap.of("key1", "value1");

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private EntityResolutionClient client;

    @Mock
    private Logger logger;

    private static ResourceModel model;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        client = mock(EntityResolutionClient.class);
        logger = mock(Logger.class);

        model = ResourceModel.builder()
                             .schemaName(SCHEMA_NAME)
                             .build();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ReadHandler handler = new ReadHandler(client);

        final List<SchemaInputAttribute> mappedInputFields = Arrays.asList(SchemaInputAttribute.builder()
                                                                                               .fieldName(FIELD_NAME)
                                                                                               .groupName(GROUP_NAME)
                                                                                               .matchKey(MATCH_KEY)
                                                                                               .type(TYPE)
                                                                                               .build(),
            SchemaInputAttribute.builder()
                                .fieldName(FIELD_NAME)
                                .groupName(GROUP_NAME)
                                .matchKey(MATCH_KEY)
                                .type(TYPE)
                                .build()
        );

        final GetSchemaMappingResponse getSchemaMappingResponse = GetSchemaMappingResponse.builder()
                                                                                          .createdAt(TIME)
                                                                                          .description(DESCRIPTION)
                                                                                          .mappedInputFields(
                                                                                              mappedInputFields)
                                                                                          .schemaArn(SCHEMA_ARN)
                                                                                          .schemaName(SCHEMA_NAME)
                                                                                          .updatedAt(TIME)
                                                                                          .hasWorkflows(HAS_WORKFLOWS)
                                                                                          .build();
        final ListTagsForResourceResponse listTagsForResourceResponse = ListTagsForResourceResponse.builder()
                                                                                                   .tags(DESIRED_TAGS)
                                                                                                   .build();

        Mockito.doReturn(getSchemaMappingResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(GetSchemaMappingRequest.class), any());
        Mockito.doReturn(listTagsForResourceResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(ListTagsForResourceRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNotNull();

        assertThat(response.getResourceModel()
                           .getDescription()).isEqualTo(DESCRIPTION);
        assertThat(response.getResourceModel()
                           .getMappedInputFields()).isEqualTo(
            Translator.translateToInternalSchemaInputAttributes(mappedInputFields));
        assertThat(response.getResourceModel()
                           .getSchemaArn()).isEqualTo(SCHEMA_ARN);
        assertThat(response.getResourceModel()
                           .getSchemaName()).isEqualTo(SCHEMA_NAME);
        assertThat(response.getResourceModel()
                           .getHasWorkflows()).isEqualTo(HAS_WORKFLOWS);
        assertThat(response.getResourceModel()
                           .getTags()).isEqualTo(Translator.mapTagsToSet(DESIRED_TAGS));

        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_throwsAccessDeniedException() {
        final ReadHandler handler = new ReadHandler(client);
        AccessDeniedException exception = AccessDeniedException.builder()
                                                               .build();

        Mockito.doThrow(exception)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(GetSchemaMappingRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        assertThrows(CfnAccessDeniedException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_throwsResourceNotFoundException() {
        final ReadHandler handler = new ReadHandler(client);
        ResourceNotFoundException exception = ResourceNotFoundException.builder()
                                                                       .build();

        Mockito.doThrow(exception)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(GetSchemaMappingRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        assertThrows(CfnNotFoundException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_throwsInternalServerException() {
        final ReadHandler handler = new ReadHandler(client);
        InternalServerException exception = InternalServerException.builder()
                                                                   .build();

        Mockito.doThrow(exception)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(GetSchemaMappingRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        assertThrows(CfnServiceInternalErrorException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_throwsValidationException() {
        final ReadHandler handler = new ReadHandler(client);
        ValidationException exception = ValidationException.builder()
                                                           .build();

        Mockito.doThrow(exception)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(GetSchemaMappingRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        assertThrows(CfnInvalidRequestException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_throwsOtherException() {
        final ReadHandler handler = new ReadHandler(client);
        ThrottlingException exception = ThrottlingException.builder()
                                                           .build();

        Mockito.doThrow(exception)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(GetSchemaMappingRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        assertThrows(CfnGeneralServiceException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }
}
