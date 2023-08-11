package software.amazon.entityresolution.schemamapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static software.amazon.entityresolution.schemamapping.CreateHandler.SCHEMA_ALREADY_EXISTS_ERROR_MESSAGE;

import com.google.common.collect.ImmutableMap;
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
import software.amazon.awssdk.services.entityresolution.model.ConflictException;
import software.amazon.awssdk.services.entityresolution.model.CreateSchemaMappingRequest;
import software.amazon.awssdk.services.entityresolution.model.CreateSchemaMappingResponse;
import software.amazon.awssdk.services.entityresolution.model.ExceedsLimitException;
import software.amazon.awssdk.services.entityresolution.model.InternalServerException;
import software.amazon.awssdk.services.entityresolution.model.SchemaInputAttribute;
import software.amazon.awssdk.services.entityresolution.model.ThrottlingException;
import software.amazon.awssdk.services.entityresolution.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    private static final String SCHEMA_NAME = "schemaName";
    private static final String DESCRIPTION = "description";
    private static final String FIELD_NAME = "fieldName";
    private static final String GROUP_NAME = "groupName";
    private static final String MATCH_KEY = "matchKey";
    private static final String TYPE = "type";
    private static final String SCHEMA_ARN = "arn:aws:entityresolution:us-east-1:123456789012:schemamapping"
        + "/schemaName";
    private static final Map<String, String> DESIRED_TAGS = ImmutableMap.of("key1", "value1", "key2", "value2");

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
        final CreateHandler handler = new CreateHandler(client);

        final CreateSchemaMappingResponse createSchemaMappingResponse = buildCreateSchemaMappingResponse();

        Mockito.doReturn(createSchemaMappingResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(CreateSchemaMappingRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .desiredResourceTags(DESIRED_TAGS)
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
            Translator.translateToInternalSchemaInputAttributes(createSchemaMappingResponse.mappedInputFields()));
        assertThat(response.getResourceModel()
                           .getSchemaArn()).isEqualTo(SCHEMA_ARN);
        assertThat(response.getResourceModel()
                           .getSchemaName()).isEqualTo(SCHEMA_NAME);

        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_withNullDesiredResourceTags() {
        final CreateHandler handler = new CreateHandler(client);

        final CreateSchemaMappingResponse createSchemaMappingResponse = buildCreateSchemaMappingResponse();

        Mockito.doReturn(createSchemaMappingResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(CreateSchemaMappingRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .desiredResourceTags(null)
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
            Translator.translateToInternalSchemaInputAttributes(createSchemaMappingResponse.mappedInputFields()));
        assertThat(response.getResourceModel()
                           .getSchemaArn()).isEqualTo(SCHEMA_ARN);
        assertThat(response.getResourceModel()
                           .getSchemaName()).isEqualTo(SCHEMA_NAME);
        assertThat(response.getResourceModel()
                           .getTags()).isNull();

        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_withEmptyDesiredResourceTags() {
        final CreateHandler handler = new CreateHandler(client);

        final CreateSchemaMappingResponse createSchemaMappingResponse = buildCreateSchemaMappingResponse();

        Mockito.doReturn(createSchemaMappingResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(CreateSchemaMappingRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .desiredResourceTags(
                                                                                        ImmutableMap.of())
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
            Translator.translateToInternalSchemaInputAttributes(createSchemaMappingResponse.mappedInputFields()));
        assertThat(response.getResourceModel()
                           .getSchemaArn()).isEqualTo(SCHEMA_ARN);
        assertThat(response.getResourceModel()
                           .getSchemaName()).isEqualTo(SCHEMA_NAME);
        assertThat(response.getResourceModel()
                           .getTags()).isNull();

        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handlerRequest_schemaMappingAlreadyExists() {
        final CreateHandler handler = new CreateHandler(client);
        ConflictException exception = ConflictException.builder()
                                                       .message(SCHEMA_ALREADY_EXISTS_ERROR_MESSAGE)
                                                       .build();

        Mockito.doThrow(exception)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(CreateSchemaMappingRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        assertThrows(CfnAlreadyExistsException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handlerRequest_throwsConflictException() {
        final CreateHandler handler = new CreateHandler(client);
        ConflictException exception = ConflictException.builder()
                                                       .message("ConflictException")
                                                       .build();

        Mockito.doThrow(exception)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(CreateSchemaMappingRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        assertThrows(CfnInvalidRequestException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_throwsAccessDeniedException() {
        final CreateHandler handler = new CreateHandler(client);
        AccessDeniedException exception = AccessDeniedException.builder()
                                                               .build();

        Mockito.doThrow(exception)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(CreateSchemaMappingRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        assertThrows(CfnAccessDeniedException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_throwsExceedsLimitException() {
        final CreateHandler handler = new CreateHandler(client);
        ExceedsLimitException exception = ExceedsLimitException.builder()
                                                               .build();

        Mockito.doThrow(exception)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(CreateSchemaMappingRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        assertThrows(CfnServiceLimitExceededException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_throwsInternalServerException() {
        final CreateHandler handler = new CreateHandler(client);
        InternalServerException exception = InternalServerException.builder()
                                                                   .build();

        Mockito.doThrow(exception)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(CreateSchemaMappingRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        assertThrows(CfnServiceInternalErrorException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_throwsValidationException() {
        final CreateHandler handler = new CreateHandler(client);
        ValidationException exception = ValidationException.builder()
                                                           .build();

        Mockito.doThrow(exception)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(CreateSchemaMappingRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        assertThrows(CfnInvalidRequestException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_throwsOtherException() {
        final CreateHandler handler = new CreateHandler(client);
        ThrottlingException exception = ThrottlingException.builder()
                                                           .build();

        Mockito.doThrow(exception)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(CreateSchemaMappingRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        assertThrows(CfnGeneralServiceException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    private CreateSchemaMappingResponse buildCreateSchemaMappingResponse() {

        final List<SchemaInputAttribute> mappedInputFields = Arrays.asList(SchemaInputAttribute.builder()
                                                                                               .fieldName(FIELD_NAME)
                                                                                               .groupName(GROUP_NAME)
                                                                                               .matchKey(MATCH_KEY)
                                                                                               .type(TYPE)
                                                                                               .build());

        return CreateSchemaMappingResponse.builder()
                                          .description(DESCRIPTION)
                                          .mappedInputFields(mappedInputFields)
                                          .schemaName(SCHEMA_NAME)
                                          .schemaArn(SCHEMA_ARN)
                                          .build();
    }
}
