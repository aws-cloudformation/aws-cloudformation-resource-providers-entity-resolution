package software.amazon.entityresolution.idmappingworkflow;

import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.entityresolution.EntityResolutionClient;
import software.amazon.awssdk.services.entityresolution.model.AccessDeniedException;
import software.amazon.awssdk.services.entityresolution.model.GetIdMappingWorkflowRequest;
import software.amazon.awssdk.services.entityresolution.model.GetIdMappingWorkflowResponse;
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

        final GetIdMappingWorkflowRequest getIdMappingWorkflowRequest = GetIdMappingWorkflowRequest.builder()
                .workflowName(
                        requestModel.getWorkflowName())
                .build();

        final ListTagsForResourceRequest listTagsForResourceRequest = ListTagsForResourceRequest.builder()
                .resourceArn(
                        Translator.toWorkflowArn(
                                request))
                .build();
        final GetIdMappingWorkflowResponse getIdMappingWorkflowResponse;

        final ListTagsForResourceResponse listTagsForResourceResponse;

        try {
            getIdMappingWorkflowResponse = proxy.injectCredentialsAndInvokeV2(getIdMappingWorkflowRequest,
                    client::getIdMappingWorkflow);
            logger.log(
                    String.format("Retrieved IdMapping Workflow with workflowName = %s", requestModel.getWorkflowName()));

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
                .createdAt(getIdMappingWorkflowResponse.createdAt()
                        .toString())
                .description(getIdMappingWorkflowResponse.description())
                .inputSourceConfig(
                        Translator.translateToCfnInputSourceConfig(
                                getIdMappingWorkflowResponse.inputSourceConfig(), getIdMappingWorkflowResponse.workflowArn()))
                .outputSourceConfig(
                        Translator.translateToCfnOutputSourceConfig(
                                getIdMappingWorkflowResponse.outputSourceConfig()))
                .idMappingTechniques(
                        Translator.translateToCfnResolutionTechniques(
                                getIdMappingWorkflowResponse.idMappingTechniques()))
                .roleArn(getIdMappingWorkflowResponse.roleArn())
                .tags(Translator.mapTagsToSet(
                        listTagsForResourceResponse.tags()))
                .updatedAt(getIdMappingWorkflowResponse.updatedAt()
                        .toString())
                .workflowArn(getIdMappingWorkflowResponse.workflowArn())
                .workflowName(getIdMappingWorkflowResponse.workflowName())
                .build();

        return ProgressEvent.defaultSuccessHandler(responseModel);
    }
}
