package software.amazon.entityresolution.schemamapping;

import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.entityresolution.EntityResolutionClient;
import software.amazon.awssdk.services.entityresolution.model.AccessDeniedException;
import software.amazon.awssdk.services.entityresolution.model.GetSchemaMappingRequest;
import software.amazon.awssdk.services.entityresolution.model.GetSchemaMappingResponse;
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

        final GetSchemaMappingRequest getSchemaMappingRequest = GetSchemaMappingRequest.builder()
                                                                                       .schemaName(
                                                                                           requestModel.getSchemaName())
                                                                                       .build();

        final ListTagsForResourceRequest listTagsForResourceRequest = ListTagsForResourceRequest.builder()
                                                                                                .resourceArn(
                                                                                                    Translator.toSchemaArn(
                                                                                                        request))
                                                                                                .build();
        final GetSchemaMappingResponse getSchemaMappingResponse;

        final ListTagsForResourceResponse listTagsForResourceResponse;

        try {
            getSchemaMappingResponse = proxy.injectCredentialsAndInvokeV2(getSchemaMappingRequest,
                client::getSchemaMapping);
            logger.log(String.format("Retrieved Schema Mapping with schemaName = %s", requestModel.getSchemaName()));

            listTagsForResourceResponse = proxy.injectCredentialsAndInvokeV2(listTagsForResourceRequest,
                client::listTagsForResource);
            logger.log(String.format("Retrieve Tags for schemaName = %s", requestModel.getSchemaName()));
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
                                                         .createdAt(getSchemaMappingResponse.createdAt()
                                                                                            .toString())
                                                         .description(getSchemaMappingResponse.description())
                                                         .mappedInputFields(
                                                             Translator.translateToInternalSchemaInputAttributes(
                                                                 getSchemaMappingResponse.mappedInputFields()))
                                                         .schemaArn(getSchemaMappingResponse.schemaArn())
                                                         .schemaName(getSchemaMappingResponse.schemaName())
                                                         .tags(Translator.mapTagsToSet(
                                                             listTagsForResourceResponse.tags()))
                                                         .updatedAt(getSchemaMappingResponse.updatedAt()
                                                                                            .toString())
                                                         .hasWorkflows(getSchemaMappingResponse.hasWorkflows())
                                                         .build();

        return ProgressEvent.defaultSuccessHandler(responseModel);
    }
}
