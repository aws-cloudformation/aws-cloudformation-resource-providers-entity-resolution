package software.amazon.entityresolution.matchingworkflow;

import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.entityresolution.EntityResolutionClient;
import software.amazon.awssdk.services.entityresolution.model.AccessDeniedException;
import software.amazon.awssdk.services.entityresolution.model.DeleteMatchingWorkflowRequest;
import software.amazon.awssdk.services.entityresolution.model.GetMatchingWorkflowRequest;
import software.amazon.awssdk.services.entityresolution.model.InternalServerException;
import software.amazon.awssdk.services.entityresolution.model.ResourceNotFoundException;
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

@NoArgsConstructor
public class DeleteHandler extends BaseHandler<CallbackContext> {

    private EntityResolutionClient client;

    public DeleteHandler(EntityResolutionClient client) {
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

        final DeleteMatchingWorkflowRequest deleteMatchingWorkflowRequest = DeleteMatchingWorkflowRequest.builder()
                                                                                                         .workflowName(
                                                                                                             requestModel.getWorkflowName())
                                                                                                         .build();

        try {
            checkIfWorkflowExists(proxy, requestModel);

            proxy.injectCredentialsAndInvokeV2(deleteMatchingWorkflowRequest,
                client::deleteMatchingWorkflow);
            logger.log(String.format("Deleted Matching Workflow with workflowName = %s", requestModel.getWorkflowName()));
        } catch (final AccessDeniedException e) {
            throw new CfnAccessDeniedException(e);
        } catch (final ResourceNotFoundException e) {
            throw new CfnNotFoundException(e);
        } catch (final InternalServerException e) {
            throw new CfnServiceInternalErrorException(e);
        } catch (final ValidationException e) {
            throw new CfnInvalidRequestException(e);
        } catch (final Exception e) {
            throw new CfnGeneralServiceException(e);
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .resourceModel(null)
                            .status(OperationStatus.SUCCESS)
                            .build();
    }

    /**
     * This helper function is used in order to pass Contract Test - test_delete_delete.
     * <p>
     * To honor handler contract, if a resource is successfully delete, a subsequent DELETE request with same parameter
     * must throw "ResourceNotFound" exception.
     */
    private void checkIfWorkflowExists(
        final AmazonWebServicesClientProxy proxy,
        final ResourceModel requestModel) {
        final GetMatchingWorkflowRequest getMatchingWorkflowRequest = GetMatchingWorkflowRequest.builder()
                                                                                                .workflowName(
                                                                                                    requestModel.getWorkflowName())
                                                                                                .build();

        try {
            proxy.injectCredentialsAndInvokeV2(getMatchingWorkflowRequest, client::getMatchingWorkflow);
        } catch (final ResourceNotFoundException e) {
            throw ResourceNotFoundException.builder()
                                           .build();
        }
    }
}
