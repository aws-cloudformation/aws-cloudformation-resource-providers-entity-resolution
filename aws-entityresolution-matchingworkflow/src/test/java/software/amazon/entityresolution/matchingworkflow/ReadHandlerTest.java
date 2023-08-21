package software.amazon.entityresolution.matchingworkflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static software.amazon.entityresolution.matchingworkflow.Translator.getNameFromArn;

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
import software.amazon.awssdk.services.entityresolution.model.GetMatchingWorkflowRequest;
import software.amazon.awssdk.services.entityresolution.model.GetMatchingWorkflowResponse;
import software.amazon.awssdk.services.entityresolution.model.InputSource;
import software.amazon.awssdk.services.entityresolution.model.InternalServerException;
import software.amazon.awssdk.services.entityresolution.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.entityresolution.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.entityresolution.model.OutputAttribute;
import software.amazon.awssdk.services.entityresolution.model.OutputSource;
import software.amazon.awssdk.services.entityresolution.model.ResolutionTechniques;
import software.amazon.awssdk.services.entityresolution.model.ResourceNotFoundException;
import software.amazon.awssdk.services.entityresolution.model.Rule;
import software.amazon.awssdk.services.entityresolution.model.RuleBasedProperties;
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
    private static final String WORKFLOW_NAME = "workflowName";
    private static final String DESCRIPTION = "description";
    private static final boolean APPLY_NORMALIZATION = true;
    private static final boolean HASHED = true;
    private static final String INPUT_SOURCE_ARN = "arn:aws:glue:us-east-1:123456789012:table/glueDb/glueTable";
    private static final String SCHEMA_ARN = "arn:aws:entityresolution:us-east-1:123456789012:schemamapping/schemaName";
    private static final String WORKFLOW_ARN = "arn:aws:entityresolution:us-east-1:123456789012:matchingworkflow"
        + "/workflowName";
    private static final String KMS_ARN = "arn:aws:kms:us-east-1:123456789012:key/487846fe-1f5c-451a-84fb-82612296f28f";
    private static final String NAME = "name";
    private static final String OUTPUT_S3_PATH = "s3://test-bucket/";
    private static final String RESOLUTION_TYPE = "RULE_MATCHING";
    private static final String ATTRIBUTE_MATCHING_MODEL = "ONE_TO_ONE";
    private static final List<String> MATCHING_KEYS = Arrays.asList("name", "address");
    private static final String RULE_NAME = "rule1";
    private static final String ROLE_ARN = "arn:aws:iam::123456789012:role/contracttest-StackRole-2EC2FS8R41SL";
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
                             .workflowName(WORKFLOW_NAME)
                             .build();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ReadHandler handler = new ReadHandler(client);

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

        final GetMatchingWorkflowResponse getMatchingWorkflowResponse = GetMatchingWorkflowResponse.builder()
                                                                                                   .createdAt(TIME)
                                                                                                   .description(
                                                                                                       DESCRIPTION)
                                                                                                   .inputSourceConfig(
                                                                                                       inputSourceConfig)
                                                                                                   .outputSourceConfig(
                                                                                                       outputSourceConfig)
                                                                                                   .resolutionTechniques(
                                                                                                       resolutionTechniques)
                                                                                                   .roleArn(ROLE_ARN)
                                                                                                   .workflowArn(
                                                                                                       WORKFLOW_ARN)
                                                                                                   .workflowName(
                                                                                                       WORKFLOW_NAME)
                                                                                                   .updatedAt(TIME)
                                                                                                   .build();

        final ListTagsForResourceResponse listTagsForResourceResponse = ListTagsForResourceResponse.builder()
                                                                                                   .tags(DESIRED_TAGS)
                                                                                                   .build();

        Mockito.doReturn(getMatchingWorkflowResponse)
               .when(proxy)
               .injectCredentialsAndInvokeV2(any(GetMatchingWorkflowRequest.class), any());
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
                           .getInputSourceConfig()).isEqualTo(
            Translator.translateToCfnInputSourceConfig(getMatchingWorkflowResponse.inputSourceConfig(),
                WORKFLOW_ARN));
        assertThat(response.getResourceModel()
                           .getOutputSourceConfig()).isEqualTo(
            Translator.translateToCfnOutputSourceConfig(getMatchingWorkflowResponse.outputSourceConfig()));
        assertThat(response.getResourceModel()
                           .getResolutionTechniques()).isEqualTo(
            Translator.translateToCfnResolutionTechniques(getMatchingWorkflowResponse.resolutionTechniques()));
        assertThat(response.getResourceModel()
                           .getRoleArn()).isEqualTo(ROLE_ARN);
        assertThat(response.getResourceModel()
                           .getWorkflowArn()).isEqualTo(WORKFLOW_ARN);
        assertThat(response.getResourceModel()
                           .getWorkflowName()).isEqualTo(WORKFLOW_NAME);
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
               .injectCredentialsAndInvokeV2(any(GetMatchingWorkflowRequest.class), any());

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
               .injectCredentialsAndInvokeV2(any(GetMatchingWorkflowRequest.class), any());

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
               .injectCredentialsAndInvokeV2(any(GetMatchingWorkflowRequest.class), any());

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
               .injectCredentialsAndInvokeV2(any(GetMatchingWorkflowRequest.class), any());

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
               .injectCredentialsAndInvokeV2(any(GetMatchingWorkflowRequest.class), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                                                                                    .desiredResourceState(model)
                                                                                    .build();

        assertThrows(CfnGeneralServiceException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }
}
