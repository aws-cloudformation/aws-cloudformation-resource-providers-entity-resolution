package software.amazon.entityresolution.matchingworkflow;

import java.util.Map;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.entityresolution.EntityResolutionClient;
import software.amazon.awssdk.services.entityresolution.model.AccessDeniedException;
import software.amazon.awssdk.services.entityresolution.model.ConflictException;
import software.amazon.awssdk.services.entityresolution.model.CreateMatchingWorkflowRequest;
import software.amazon.awssdk.services.entityresolution.model.CreateMatchingWorkflowResponse;
import software.amazon.awssdk.services.entityresolution.model.ExceedsLimitException;
import software.amazon.awssdk.services.entityresolution.model.GetMatchingWorkflowRequest;
import software.amazon.awssdk.services.entityresolution.model.InternalServerException;
import software.amazon.awssdk.services.entityresolution.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@NoArgsConstructor
public class CreateHandler extends BaseHandler<CallbackContext> {

    public static final String WORKFLOW_ALREADY_EXISTS_ERROR_MESSAGE = "MatchingWorkflow already exists";

    private EntityResolutionClient client;

    public CreateHandler(EntityResolutionClient client) {
        this.client = client;
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        if (this.client == null) {
            this.client = ClientBuilder.getClient();
        }

        final ResourceModel requestModel = request.getDesiredResourceState();

        final Map<String, String> resourceTags;
        if (request.getDesiredResourceTags() == null || request.getDesiredResourceTags()
                                                               .isEmpty()) {
            resourceTags = null;
        } else {
            resourceTags = request.getDesiredResourceTags();
        }

        final CreateMatchingWorkflowRequest createMatchingWorkflowRequest = CreateMatchingWorkflowRequest.builder()
                                                                                                         .description(
                                                                                                             requestModel.getDescription())
                                                                                                         .inputSourceConfig(
                                                                                                             Translator.translateToVeniceInputSourceConfig(
                                                                                                                 requestModel.getInputSourceConfig()))
                                                                                                         .outputSourceConfig(
                                                                                                             Translator.translateToVeniceOutputSourceConfig(
                                                                                                                 requestModel.getOutputSourceConfig()))
                                                                                                         .resolutionTechniques(
                                                                                                             Translator.translateToVeniceResolutionTechniques(
                                                                                                                 requestModel.getResolutionTechniques()))
                                                                                                         .roleArn(
                                                                                                             requestModel.getRoleArn())
                                                                                                         .workflowName(
                                                                                                             requestModel.getWorkflowName())
                                                                                                         .tags(
                                                                                                             resourceTags)
                                                                                                         .build();

        final CreateMatchingWorkflowResponse createMatchingWorkflowResponse;

        try {
            checkIfWorkflowExists(proxy, requestModel);

            createMatchingWorkflowResponse = proxy.injectCredentialsAndInvokeV2(createMatchingWorkflowRequest,
                client::createMatchingWorkflow);

            logger.log(
                String.format("Created Matching Workflow with workflowName = %s", requestModel.getWorkflowName()));
        } catch (final ConflictException e) {
            if (e.getMessage() != null && e.getMessage()
                                           .contains(WORKFLOW_ALREADY_EXISTS_ERROR_MESSAGE)) {
                throw new CfnAlreadyExistsException(e);
            }
            throw new CfnInvalidRequestException(e);
        } catch (final AccessDeniedException e) {
            throw new CfnAccessDeniedException(e);
        } catch (final ExceedsLimitException e) {
            throw new CfnServiceLimitExceededException(e);
        } catch (final InternalServerException e) {
            throw new CfnServiceInternalErrorException(e);
        } catch (final ValidationException e) {
            throw new CfnInvalidRequestException(e);
        } catch (final Exception e) {
            throw new CfnGeneralServiceException(e);
        }

        final ResourceModel responseModel = ResourceModel.builder()
                                                         .description(createMatchingWorkflowResponse.description())
                                                         .inputSourceConfig(Translator.translateToCfnInputSourceConfig(
                                                             createMatchingWorkflowResponse.inputSourceConfig(), createMatchingWorkflowResponse.workflowArn()))
                                                         .outputSourceConfig(Translator.translateToCfnOutputSourceConfig(
                                                             createMatchingWorkflowResponse.outputSourceConfig()))
                                                         .resolutionTechniques(Translator.translateToCfnResolutionTechniques(
                                                             createMatchingWorkflowResponse.resolutionTechniques()))
                                                         .roleArn(createMatchingWorkflowResponse.roleArn())
                                                         .workflowArn(createMatchingWorkflowResponse.workflowArn())
                                                         .workflowName(createMatchingWorkflowResponse.workflowName())
                                                         .build();

        return ProgressEvent.defaultSuccessHandler(responseModel);
    }

    /**
     * This helper function is used in order to pass Contract Test - test_create_create.
     * <p>
     * To honor handler contract, if a resource is successfully created, a subsequent CREATE request with same
     * parameters must throw "AlreadyExists" exception.
     */
    private void checkIfWorkflowExists(
        final AmazonWebServicesClientProxy proxy,
        final ResourceModel requestModel) {

        final GetMatchingWorkflowRequest getSchemaMappingRequest = GetMatchingWorkflowRequest.builder()
                                                                                             .workflowName(
                                                                                                 requestModel
                                                                                                     .getWorkflowName())
                                                                                             .build();

        try {
            proxy.injectCredentialsAndInvokeV2(getSchemaMappingRequest, client::getMatchingWorkflow);
        } catch (final Exception e) {
            return;
        }
        throw ConflictException.builder()
                               .message(WORKFLOW_ALREADY_EXISTS_ERROR_MESSAGE)
                               .build();
    }
}
