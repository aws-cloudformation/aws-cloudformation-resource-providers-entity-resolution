package software.amazon.entityresolution.matchingworkflow;

import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.entityresolution.EntityResolutionClient;
import software.amazon.awssdk.services.entityresolution.model.AccessDeniedException;
import software.amazon.awssdk.services.entityresolution.model.GetMatchingWorkflowRequest;
import software.amazon.awssdk.services.entityresolution.model.GetMatchingWorkflowResponse;
import software.amazon.awssdk.services.entityresolution.model.InternalServerException;
import software.amazon.awssdk.services.entityresolution.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.entityresolution.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.entityresolution.model.ResourceNotFoundException;
import software.amazon.awssdk.services.entityresolution.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@NoArgsConstructor
public class ReadHandler extends BaseHandler<CallbackContext> {

    private EntityResolutionClient client;

    public ReadHandler(EntityResolutionClient client) {
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

        final GetMatchingWorkflowRequest getMatchingWorkflowRequest = GetMatchingWorkflowRequest.builder()
                                                                                                .workflowName(
                                                                                                    requestModel.getWorkflowName())
                                                                                                .build();

        final ListTagsForResourceRequest listTagsForResourceRequest = ListTagsForResourceRequest.builder()
                                                                                                .resourceArn(
                                                                                                    Translator.toWorkflowArn(
                                                                                                        request))
                                                                                                .build();
        final GetMatchingWorkflowResponse getMatchingWorkflowResponse;

        final ListTagsForResourceResponse listTagsForResourceResponse;

        try {
            getMatchingWorkflowResponse = proxy.injectCredentialsAndInvokeV2(getMatchingWorkflowRequest,
                client::getMatchingWorkflow);
            logger.log(
                String.format("Retrieved Matching Workflow with workflowName = %s", requestModel.getWorkflowName()));

            listTagsForResourceResponse = proxy.injectCredentialsAndInvokeV2(listTagsForResourceRequest,
                client::listTagsForResource);
            logger.log(String.format("Retrieve Tags for workflowName = %s", requestModel.getWorkflowName()));
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

        final ResourceModel responseModel = ResourceModel.builder()
                                                         .createdAt(getMatchingWorkflowResponse.createdAt()
                                                                                               .toString())
                                                         .description(getMatchingWorkflowResponse.description())
                                                         .inputSourceConfig(
                                                             Translator.translateToCfnInputSourceConfig(
                                                                 getMatchingWorkflowResponse.inputSourceConfig(), getMatchingWorkflowResponse.workflowArn()))
                                                         .outputSourceConfig(
                                                             Translator.translateToCfnOutputSourceConfig(
                                                                 getMatchingWorkflowResponse.outputSourceConfig()))
                                                         .resolutionTechniques(
                                                             Translator.translateToCfnResolutionTechniques(
                                                                 getMatchingWorkflowResponse.resolutionTechniques()))
                                                         .roleArn(getMatchingWorkflowResponse.roleArn())
                                                         .tags(Translator.mapTagsToSet(
                                                             listTagsForResourceResponse.tags()))
                                                         .updatedAt(getMatchingWorkflowResponse.updatedAt()
                                                                                               .toString())
                                                         .workflowArn(getMatchingWorkflowResponse.workflowArn())
                                                         .workflowName(getMatchingWorkflowResponse.workflowName())
                                                         .build();

        return ProgressEvent.defaultSuccessHandler(responseModel);
    }
}
