
package software.amazon.entityresolution.matchingworkflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import static software.amazon.entityresolution.matchingworkflow.CreateHandler.WORKFLOW_ALREADY_EXISTS_ERROR_MESSAGE;
import static software.amazon.entityresolution.matchingworkflow.Translator.getNameFromArn;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.document.Document;
import software.amazon.awssdk.services.entityresolution.EntityResolutionClient;
import software.amazon.awssdk.services.entityresolution.model.AccessDeniedException;
import software.amazon.awssdk.services.entityresolution.model.ConflictException;
import software.amazon.awssdk.services.entityresolution.model.CreateMatchingWorkflowRequest;
import software.amazon.awssdk.services.entityresolution.model.CreateMatchingWorkflowResponse;
import software.amazon.awssdk.services.entityresolution.model.ExceedsLimitException;
import software.amazon.awssdk.services.entityresolution.model.InputSource;
import software.amazon.awssdk.services.entityresolution.model.InternalServerException;
import software.amazon.awssdk.services.entityresolution.model.OutputAttribute;
import software.amazon.awssdk.services.entityresolution.model.OutputSource;
import software.amazon.awssdk.services.entityresolution.model.ResolutionTechniques;
import software.amazon.awssdk.services.entityresolution.model.Rule;
import software.amazon.awssdk.services.entityresolution.model.RuleBasedProperties;
import software.amazon.awssdk.services.entityresolution.model.ProviderProperties;
import software.amazon.awssdk.services.entityresolution.model.IntermediateSourceConfiguration;
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

import javax.print.Doc;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    private static final String WORKFLOW_NAME = "workflowName";
    private static final String DESCRIPTION = "description";
    private static final boolean APPLY_NORMALIZATION = true;
    private static final boolean HASHED = true;
    private static final String INPUT_SOURCE_ARN = "arn:aws:glue:us-east-1:123456789012:table/glueDb/glueTable";
    private static final String PROVIDER_SERVICE_ARN = "arn:aws:entityresolution:us-east-1:providerservice/liveramp/assignment";
    private static final String SCHEMA_ARN = "arn:aws:entityresolution:us-east-1:123456789012:schemamapping/schemaName";
    private static final String WORKFLOW_ARN = "arn:aws:entityresolution:us-east-1:123456789012:matchingworkflow"
        + "/workflowName";
    private static final String KMS_ARN = "arn:aws:kms:us-east-1:123456789012:key/487846fe-1f5c-451a-84fb-82612296f28f";
    private static final String NAME = "name";
    private static final String OUTPUT_S3_PATH = "s3://test-bucket/";
    private static final String INTERMEDIATE_S3_PATH = "s3://intermediate-bucket/folder";
    private static final String RESOLUTION_TYPE = "RULE_MATCHING";
    private static final String RESOLUTION_TYPE_PROVIDER = "PROVIDER";
    private static final String ATTRIBUTE_MATCHING_MODEL = "ONE_TO_ONE";
    private static final List<String> MATCHING_KEYS = Arrays.asList("name", "address");
    private static final String RULE_NAME = "rule1";
    private static final String ROLE_ARN = "arn:aws:iam::123456789012:role/contracttest-StackRole-2EC2FS8R41SL";
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
                             .workflowName(WORKFLOW_NAME)
                             .build();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final CreateHandler handler = new CreateHandler(client);

        final CreateMatchingWorkflowResponse createMatchingWorkflowResponse = buildCreateMatchingWorkflowResponse();

        Mockito.doReturn(createMatchingWorkflowResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(CreateMatchingWorkflowRequest.class), any());

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
                           .getInputSourceConfig()).isEqualTo(
            Translator.translateToCfnInputSourceConfig(createMatchingWorkflowResponse.inputSourceConfig(),
                WORKFLOW_ARN));
        assertThat(response.getResourceModel()
                           .getOutputSourceConfig()).isEqualTo(
            Translator.translateToCfnOutputSourceConfig(createMatchingWorkflowResponse.outputSourceConfig()));
        assertThat(response.getResourceModel()
                           .getResolutionTechniques()).isEqualTo(
            Translator.translateToCfnResolutionTechniques(createMatchingWorkflowResponse.resolutionTechniques()));
        assertThat(response.getResourceModel()
                           .getRoleArn()).isEqualTo(ROLE_ARN);
        assertThat(response.getResourceModel()
                           .getWorkflowArn()).isEqualTo(WORKFLOW_ARN);
        assertThat(response.getResourceModel()
                           .getWorkflowName()).isEqualTo(WORKFLOW_NAME);

        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_PartnerWorkflows_SimpleSuccess() {
        final CreateHandler handler = new CreateHandler(client);

        final CreateMatchingWorkflowResponse createMatchingWorkflowResponse = buildCreatePartnerMatchingWorkflowResponse();

        Mockito.doReturn(createMatchingWorkflowResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(CreateMatchingWorkflowRequest.class), any());

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
                           .getInputSourceConfig()).isEqualTo(
            Translator.translateToCfnInputSourceConfig(createMatchingWorkflowResponse.inputSourceConfig(),
                WORKFLOW_ARN));
        assertThat(response.getResourceModel()
                           .getOutputSourceConfig()).isEqualTo(
            Translator.translateToCfnOutputSourceConfig(createMatchingWorkflowResponse.outputSourceConfig()));
        assertThat(response.getResourceModel()
                           .getResolutionTechniques()).isEqualTo(
            Translator.translateToCfnResolutionTechniques(createMatchingWorkflowResponse.resolutionTechniques()));
        assertThat(response.getResourceModel()
                           .getRoleArn()).isEqualTo(ROLE_ARN);
        assertThat(response.getResourceModel()
                           .getWorkflowArn()).isEqualTo(WORKFLOW_ARN);
        assertThat(response.getResourceModel()
                           .getWorkflowName()).isEqualTo(WORKFLOW_NAME);

        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_withNullDesiredResourceTags() {
        final CreateHandler handler = new CreateHandler(client);

        final CreateMatchingWorkflowResponse createMatchingWorkflowResponse = buildCreateMatchingWorkflowResponse();

        Mockito.doReturn(createMatchingWorkflowResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(CreateMatchingWorkflowRequest.class), any());

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
                           .getInputSourceConfig()).isEqualTo(
            Translator.translateToCfnInputSourceConfig(createMatchingWorkflowResponse.inputSourceConfig(),
                WORKFLOW_ARN));
        assertThat(response.getResourceModel()
                           .getOutputSourceConfig()).isEqualTo(
            Translator.translateToCfnOutputSourceConfig(createMatchingWorkflowResponse.outputSourceConfig()));
        assertThat(response.getResourceModel()
                           .getResolutionTechniques()).isEqualTo(
            Translator.translateToCfnResolutionTechniques(createMatchingWorkflowResponse.resolutionTechniques()));
        assertThat(response.getResourceModel()
                           .getRoleArn()).isEqualTo(ROLE_ARN);
        assertThat(response.getResourceModel()
                           .getWorkflowArn()).isEqualTo(WORKFLOW_ARN);
        assertThat(response.getResourceModel()
                           .getWorkflowName()).isEqualTo(WORKFLOW_NAME);
        assertThat(response.getResourceModel()
                           .getTags()).isNull();

        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_withEmptyDesiredResourceTags() {
        final CreateHandler handler = new CreateHandler(client);

        final CreateMatchingWorkflowResponse createMatchingWorkflowResponse = buildCreateMatchingWorkflowResponse();

        Mockito.doReturn(createMatchingWorkflowResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(CreateMatchingWorkflowRequest.class), any());

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
                           .getInputSourceConfig()).isEqualTo(
            Translator.translateToCfnInputSourceConfig(createMatchingWorkflowResponse.inputSourceConfig(),
                WORKFLOW_ARN));
        assertThat(response.getResourceModel()
                           .getOutputSourceConfig()).isEqualTo(
            Translator.translateToCfnOutputSourceConfig(createMatchingWorkflowResponse.outputSourceConfig()));
        assertThat(response.getResourceModel()
                           .getResolutionTechniques()).isEqualTo(
            Translator.translateToCfnResolutionTechniques(createMatchingWorkflowResponse.resolutionTechniques()));
        assertThat(response.getResourceModel()
                           .getRoleArn()).isEqualTo(ROLE_ARN);
        assertThat(response.getResourceModel()
                           .getWorkflowArn()).isEqualTo(WORKFLOW_ARN);
        assertThat(response.getResourceModel()
                           .getWorkflowName()).isEqualTo(WORKFLOW_NAME);
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
                                                       .message(WORKFLOW_ALREADY_EXISTS_ERROR_MESSAGE)
                                                       .build();

        Mockito.doThrow(exception)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(CreateMatchingWorkflowRequest.class), any());

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
               .injectCredentialsAndInvokeV2(any(CreateMatchingWorkflowRequest.class), any());

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
               .injectCredentialsAndInvokeV2(any(CreateMatchingWorkflowRequest.class), any());

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
               .injectCredentialsAndInvokeV2(any(CreateMatchingWorkflowRequest.class), any());

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
               .injectCredentialsAndInvokeV2(any(CreateMatchingWorkflowRequest.class), any());

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
               .injectCredentialsAndInvokeV2(any(CreateMatchingWorkflowRequest.class), any());

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
               .injectCredentialsAndInvokeV2(any(CreateMatchingWorkflowRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        assertThrows(CfnGeneralServiceException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    private CreateMatchingWorkflowResponse buildCreateMatchingWorkflowResponse() {

        final List<InputSource> inputSourceConfig = Arrays.asList(InputSource.builder()
                                                                             .applyNormalization(APPLY_NORMALIZATION)
                                                                             .inputSourceARN(INPUT_SOURCE_ARN)
                                                                             .schemaName(getNameFromArn(SCHEMA_ARN))
                                                                             .build());

        final List<OutputAttribute> output = Arrays.asList(OutputAttribute.builder()
                                                                          .hashed(HASHED)
                                                                          .name(NAME)
                                                                          .build());
        final List<OutputSource> outputSourceConfig = Arrays.asList(OutputSource.builder()
                                                                                .applyNormalization(APPLY_NORMALIZATION)
                                                                                .kmsArn(KMS_ARN)
                                                                                .output(output)
                                                                                .outputS3Path(OUTPUT_S3_PATH)
                                                                                .build());

        final List<Rule> rules = Arrays.asList(Rule.builder()
                                                   .matchingKeys(MATCHING_KEYS)
                                                   .ruleName(RULE_NAME)
                                                   .build());
        final RuleBasedProperties ruleBasedProperties = RuleBasedProperties.builder()
                                                                           .attributeMatchingModel(
                                                                               ATTRIBUTE_MATCHING_MODEL)
                                                                           .rules(rules)
                                                                           .build();
        final ResolutionTechniques resolutionTechniques = ResolutionTechniques.builder()
                                                                              .resolutionType(RESOLUTION_TYPE)
                                                                              .ruleBasedProperties(ruleBasedProperties)
                                                                              .build();

        return CreateMatchingWorkflowResponse.builder()
                                             .description(DESCRIPTION)
                                             .inputSourceConfig(inputSourceConfig)
                                             .outputSourceConfig(outputSourceConfig)
                                             .resolutionTechniques(resolutionTechniques)
                                             .roleArn(ROLE_ARN)
                                             .workflowArn(WORKFLOW_ARN)
                                             .workflowName(WORKFLOW_NAME)
                                             .build();
    }

    private CreateMatchingWorkflowResponse buildCreatePartnerMatchingWorkflowResponse() {

        final List<InputSource> inputSourceConfig = Arrays.asList(InputSource.builder()
                                                                             .applyNormalization(APPLY_NORMALIZATION)
                                                                             .inputSourceARN(INPUT_SOURCE_ARN)
                                                                             .schemaName(getNameFromArn(SCHEMA_ARN))
                                                                             .build());

        final List<OutputAttribute> output = Arrays.asList(OutputAttribute.builder()
                                                                          .hashed(HASHED)
                                                                          .name(NAME)
                                                                          .build());
        final List<OutputSource> outputSourceConfig = Arrays.asList(OutputSource.builder()
                                                                                .applyNormalization(APPLY_NORMALIZATION)
                                                                                .kmsArn(KMS_ARN)
                                                                                .output(output)
                                                                                .outputS3Path(OUTPUT_S3_PATH)
                                                                                .build());

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", "value2");


        Map<String, Document> documentMap = attributes
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> Document.fromString(entry.getValue().toString())));

        Document document = Document.fromMap(documentMap);


        final ProviderProperties providerProperties = ProviderProperties.builder()
                                                                        .providerServiceArn(PROVIDER_SERVICE_ARN)
                                                                        .providerConfiguration(document)
                                                                        .intermediateSourceConfiguration(IntermediateSourceConfiguration.builder()
                                                                                                                                        .intermediateS3Path(INTERMEDIATE_S3_PATH)
                                                                                                                                        .build())
                                                                        .build();

        final ResolutionTechniques resolutionTechniques = ResolutionTechniques.builder()
                                                                              .resolutionType(RESOLUTION_TYPE_PROVIDER)
                                                                              .providerProperties(providerProperties)
                                                                              .build();

        return CreateMatchingWorkflowResponse.builder()
                                             .description(DESCRIPTION)
                                             .inputSourceConfig(inputSourceConfig)
                                             .outputSourceConfig(outputSourceConfig)
                                             .resolutionTechniques(resolutionTechniques)
                                             .roleArn(ROLE_ARN)
                                             .workflowArn(WORKFLOW_ARN)
                                             .workflowName(WORKFLOW_NAME)
                                             .build();
    }
}
