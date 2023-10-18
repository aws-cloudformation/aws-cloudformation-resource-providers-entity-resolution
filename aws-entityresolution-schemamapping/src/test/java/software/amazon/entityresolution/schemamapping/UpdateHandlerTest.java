package software.amazon.entityresolution.schemamapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.regions.Regions;
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
import software.amazon.awssdk.services.entityresolution.model.TagResourceRequest;
import software.amazon.awssdk.services.entityresolution.model.TagResourceResponse;
import software.amazon.awssdk.services.entityresolution.model.ThrottlingException;
import software.amazon.awssdk.services.entityresolution.model.UntagResourceRequest;
import software.amazon.awssdk.services.entityresolution.model.UntagResourceResponse;
import software.amazon.awssdk.services.entityresolution.model.UpdateSchemaMappingRequest;
import software.amazon.awssdk.services.entityresolution.model.UpdateSchemaMappingResponse;
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
public class UpdateHandlerTest {

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
    private static final Map<String, String> PREVIOUS_TAGS = ImmutableMap.of("key1", "value1");
    private static final Map<String, String> DESIRED_TAGS = ImmutableMap.of("key1", "value1");

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private EntityResolutionClient client;

    @Mock
    private Logger logger;

    private static ResourceModel model;

    private GetSchemaMappingResponse getSchemaMappingResponse;

    @BeforeEach
    public void setup() {
        System.setProperty(SDKGlobalConfiguration.AWS_REGION_SYSTEM_PROPERTY, Regions.US_EAST_1.getName());

        proxy = mock(AmazonWebServicesClientProxy.class);
        client = mock(EntityResolutionClient.class);
        logger = mock(Logger.class);

        model = ResourceModel.builder()
                             .schemaName(SCHEMA_NAME)
                             .build();

        final List<SchemaInputAttribute> mappedInputFields = Arrays.asList(SchemaInputAttribute.builder()
                                                                                               .fieldName(FIELD_NAME)
                                                                                               .groupName(GROUP_NAME)
                                                                                               .matchKey(MATCH_KEY)
                                                                                               .type(TYPE)
                                                                                               .build());

        getSchemaMappingResponse = GetSchemaMappingResponse.builder()
                                                           .createdAt(TIME)
                                                           .description(DESCRIPTION)
                                                           .mappedInputFields(mappedInputFields)
                                                           .schemaArn(SCHEMA_ARN)
                                                           .schemaName(SCHEMA_NAME)
                                                           .updatedAt(TIME)
                                                           .hasWorkflows(HAS_WORKFLOWS)
                                                           .build();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final UpdateHandler handler = new UpdateHandler(client);

        final UpdateSchemaMappingResponse updateSchemaMappingResponse = buildUpdateSchemaMappingResponse();

        Mockito.doReturn(updateSchemaMappingResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(UpdateSchemaMappingRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .previousResourceTags(PREVIOUS_TAGS)
                                                                                    .desiredResourceTags(DESIRED_TAGS)
                                                                                    .build();

        Mockito.doReturn(getSchemaMappingResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(GetSchemaMappingRequest.class), any());

        Mockito.doReturn(ListTagsForResourceResponse.builder()
                                                    .tags(ImmutableMap.of("key1", "value1"))
                                                    .build())
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(ListTagsForResourceRequest.class), any());

        Mockito.doReturn(UntagResourceResponse.builder()
                                              .build())
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(UntagResourceRequest.class), any());

        Mockito.doReturn(TagResourceResponse.builder()
                                            .build())
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(TagResourceRequest.class), any());

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, logger);

        Mockito.verify(proxy)
               .injectCredentialsAndInvokeV2(any(UntagResourceRequest.class), any());
        Mockito.verify(proxy)
               .injectCredentialsAndInvokeV2(any(TagResourceRequest.class), any());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModel()
                           .getSchemaArn()).isEqualTo(updateSchemaMappingResponse.schemaArn());
        assertThat(response.getResourceModel()
                           .getDescription()).isEqualTo(updateSchemaMappingResponse.description());
        assertThat(response.getResourceModel()
                           .getMappedInputFields()).isEqualTo(
            Translator.translateToInternalSchemaInputAttributes(updateSchemaMappingResponse.mappedInputFields()));
        assertThat(response.getResourceModel()
                           .getSchemaName()).isEqualTo(updateSchemaMappingResponse.schemaName());
        assertThat(response.getResourceModel()
                           .getTags()).isEqualTo(Translator.mapTagsToSet(DESIRED_TAGS));
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_withNullPreviousResourceTags() {
        final UpdateHandler handler = new UpdateHandler(client);

        final UpdateSchemaMappingResponse updateSchemaMappingResponse = buildUpdateSchemaMappingResponse();

        Mockito.doReturn(updateSchemaMappingResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(UpdateSchemaMappingRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .desiredResourceTags(DESIRED_TAGS)
                                                                                    .build();

        Mockito.doReturn(getSchemaMappingResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(GetSchemaMappingRequest.class), any());

        Mockito.doReturn(ListTagsForResourceResponse.builder()
                                                    .tags(ImmutableMap.of("key1", "value1"))
                                                    .build())
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(ListTagsForResourceRequest.class), any());

        Mockito.doReturn(TagResourceResponse.builder()
                                            .build())
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(TagResourceRequest.class), any());

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, logger);

        Mockito.verify(proxy, Mockito.times(0))
               .injectCredentialsAndInvokeV2(any(UntagResourceRequest.class), any());
        Mockito.verify(proxy)
               .injectCredentialsAndInvokeV2(any(TagResourceRequest.class), any());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()
                           .getSchemaArn()).isEqualTo(updateSchemaMappingResponse.schemaArn());
        assertThat(response.getResourceModel()
                           .getDescription()).isEqualTo(updateSchemaMappingResponse.description());
        assertThat(response.getResourceModel()
                           .getMappedInputFields()).isEqualTo(
            Translator.translateToInternalSchemaInputAttributes(updateSchemaMappingResponse.mappedInputFields()));
        assertThat(response.getResourceModel()
                           .getSchemaName()).isEqualTo(updateSchemaMappingResponse.schemaName());
        assertThat(response.getResourceModel()
                           .getTags()).isEqualTo(Translator.mapTagsToSet(DESIRED_TAGS));
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_withEmptyPreviousResourceTags() {
        final UpdateHandler handler = new UpdateHandler(client);

        final UpdateSchemaMappingResponse updateSchemaMappingResponse = buildUpdateSchemaMappingResponse();

        Mockito.doReturn(updateSchemaMappingResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(UpdateSchemaMappingRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .desiredResourceTags(DESIRED_TAGS)
                                                                                    .previousResourceTags(
                                                                                        ImmutableMap.of())
                                                                                    .build();

        Mockito.doReturn(getSchemaMappingResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(GetSchemaMappingRequest.class), any());

        Mockito.doReturn(ListTagsForResourceResponse.builder()
                                                    .tags(ImmutableMap.of("key1", "value1"))
                                                    .build())
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(ListTagsForResourceRequest.class), any());

        Mockito.doReturn(TagResourceResponse.builder()
                                            .build())
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(TagResourceRequest.class), any());

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, logger);

        Mockito.verify(proxy, Mockito.times(0))
               .injectCredentialsAndInvokeV2(any(UntagResourceRequest.class), any());
        Mockito.verify(proxy)
               .injectCredentialsAndInvokeV2(any(TagResourceRequest.class), any());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()
                           .getSchemaArn()).isEqualTo(updateSchemaMappingResponse.schemaArn());
        assertThat(response.getResourceModel()
                           .getDescription()).isEqualTo(updateSchemaMappingResponse.description());
        assertThat(response.getResourceModel()
                           .getMappedInputFields()).isEqualTo(
            Translator.translateToInternalSchemaInputAttributes(updateSchemaMappingResponse.mappedInputFields()));
        assertThat(response.getResourceModel()
                           .getSchemaName()).isEqualTo(updateSchemaMappingResponse.schemaName());
        assertThat(response.getResourceModel()
                           .getTags()).isEqualTo(Translator.mapTagsToSet(DESIRED_TAGS));
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_withNullDesiredResourceTags() {
        final UpdateHandler handler = new UpdateHandler(client);

        final UpdateSchemaMappingResponse updateSchemaMappingResponse = buildUpdateSchemaMappingResponse();

        Mockito.doReturn(updateSchemaMappingResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(UpdateSchemaMappingRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .previousResourceTags(PREVIOUS_TAGS)
                                                                                    .build();

        final SchemaInputAttribute testAttribute = SchemaInputAttribute.builder()
                                                                       .fieldName(FIELD_NAME)
                                                                       .groupName(GROUP_NAME)
                                                                       .matchKey(MATCH_KEY)
                                                                       .type(TYPE)
                                                                       .build();

        final List<SchemaInputAttribute> mappedInputFields = Arrays.asList(testAttribute);

        final GetSchemaMappingResponse getSchemaMappingResponse = GetSchemaMappingResponse.builder()
                                                                                          .createdAt(TIME)
                                                                                          .description(DESCRIPTION)
                                                                                          .mappedInputFields(
                                                                                              mappedInputFields)
                                                                                          .schemaArn(SCHEMA_ARN)
                                                                                          .schemaName(SCHEMA_NAME)
                                                                                          .tags(null)
                                                                                          .updatedAt(TIME)
                                                                                          .hasWorkflows(HAS_WORKFLOWS)
                                                                                          .build();
        Mockito.doReturn(getSchemaMappingResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(GetSchemaMappingRequest.class), any());

        Mockito.doReturn(ListTagsForResourceResponse.builder()
                                                    .build())
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(ListTagsForResourceRequest.class), any());

        Mockito.doReturn(UntagResourceResponse.builder()
                                              .build())
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(UntagResourceRequest.class), any());

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, logger);

        Mockito.verify(proxy)
               .injectCredentialsAndInvokeV2(any(UntagResourceRequest.class), any());
        Mockito.verify(proxy, Mockito.times(0))
               .injectCredentialsAndInvokeV2(any(TagResourceRequest.class), any());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()
                           .getSchemaArn()).isEqualTo(updateSchemaMappingResponse.schemaArn());
        assertThat(response.getResourceModel()
                           .getDescription()).isEqualTo(updateSchemaMappingResponse.description());
        assertThat(response.getResourceModel()
                           .getMappedInputFields()).isEqualTo(
            Translator.translateToInternalSchemaInputAttributes(updateSchemaMappingResponse.mappedInputFields()));
        assertThat(response.getResourceModel()
                           .getSchemaName()).isEqualTo(updateSchemaMappingResponse.schemaName());
        assertThat(response.getResourceModel()
                           .getTags()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_withEmptyDesiredResourceTags() {
        final UpdateHandler handler = new UpdateHandler(client);

        final UpdateSchemaMappingResponse updateSchemaMappingResponse = buildUpdateSchemaMappingResponse();

        Mockito.doReturn(updateSchemaMappingResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(UpdateSchemaMappingRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .previousResourceTags(PREVIOUS_TAGS)
                                                                                    .desiredResourceTags(
                                                                                        ImmutableMap.of())
                                                                                    .build();

        final SchemaInputAttribute testAttribute = SchemaInputAttribute.builder()
                                                                       .fieldName(FIELD_NAME)
                                                                       .groupName(GROUP_NAME)
                                                                       .matchKey(MATCH_KEY)
                                                                       .type(TYPE)
                                                                       .build();

        final List<SchemaInputAttribute> mappedInputFields = Arrays.asList(testAttribute);

        final GetSchemaMappingResponse getSchemaMappingResponse = GetSchemaMappingResponse.builder()
                                                                                          .createdAt(TIME)
                                                                                          .description(DESCRIPTION)
                                                                                          .mappedInputFields(
                                                                                              mappedInputFields)
                                                                                          .schemaArn(SCHEMA_ARN)
                                                                                          .schemaName(SCHEMA_NAME)
                                                                                          .tags(null)
                                                                                          .updatedAt(TIME)
                                                                                          .hasWorkflows(HAS_WORKFLOWS)
                                                                                          .build();
        Mockito.doReturn(getSchemaMappingResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(GetSchemaMappingRequest.class), any());

        Mockito.doReturn(ListTagsForResourceResponse.builder()
                                                    .build())
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(ListTagsForResourceRequest.class), any());

        Mockito.doReturn(UntagResourceResponse.builder()
                                              .build())
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(UntagResourceRequest.class), any());

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, null, logger);

        Mockito.verify(proxy)
               .injectCredentialsAndInvokeV2(any(UntagResourceRequest.class), any());
        Mockito.verify(proxy, Mockito.times(0))
               .injectCredentialsAndInvokeV2(any(TagResourceRequest.class), any());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()
                           .getSchemaArn()).isEqualTo(updateSchemaMappingResponse.schemaArn());
        assertThat(response.getResourceModel()
                           .getDescription()).isEqualTo(updateSchemaMappingResponse.description());
        assertThat(response.getResourceModel()
                           .getMappedInputFields()).isEqualTo(
            Translator.translateToInternalSchemaInputAttributes(updateSchemaMappingResponse.mappedInputFields()));
        assertThat(response.getResourceModel()
                           .getSchemaName()).isEqualTo(updateSchemaMappingResponse.schemaName());
        assertThat(response.getResourceModel()
                           .getTags()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_getRequestThrowsAccessDeniedException() {
        final UpdateHandler handler = new UpdateHandler(client);
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
    public void handleRequest_getRequestThrowsResourceNotFoundException() {
        final UpdateHandler handler = new UpdateHandler(client);
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
    public void handleRequest_getRequestThrowsInternalServerException() {
        final UpdateHandler handler = new UpdateHandler(client);
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
    public void handleRequest_getRequestThrowsValidationException() {
        final UpdateHandler handler = new UpdateHandler(client);
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
    public void handleRequest_getRequestThrowsOtherException() {
        final UpdateHandler handler = new UpdateHandler(client);
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

    private UpdateSchemaMappingResponse buildUpdateSchemaMappingResponse() {
        final SchemaInputAttribute testAttribute = SchemaInputAttribute.builder()
                                                                       .fieldName(FIELD_NAME)
                                                                       .groupName(GROUP_NAME)
                                                                       .matchKey(MATCH_KEY)
                                                                       .type(TYPE)
                                                                       .build();

        final List<SchemaInputAttribute> mappedInputFields = Arrays.asList(testAttribute);

        return UpdateSchemaMappingResponse.builder()
                                          .description(DESCRIPTION)
                                          .mappedInputFields(mappedInputFields)
                                          .schemaArn(SCHEMA_ARN)
                                          .schemaName(SCHEMA_NAME)
                                          .build();
    }
}
