package software.amazon.entityresolution.schemamapping;

import java.util.Map;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.entityresolution.EntityResolutionClient;
import software.amazon.awssdk.services.entityresolution.model.AccessDeniedException;
import software.amazon.awssdk.services.entityresolution.model.ConflictException;
import software.amazon.awssdk.services.entityresolution.model.CreateSchemaMappingRequest;
import software.amazon.awssdk.services.entityresolution.model.CreateSchemaMappingResponse;
import software.amazon.awssdk.services.entityresolution.model.ExceedsLimitException;
import software.amazon.awssdk.services.entityresolution.model.GetSchemaMappingRequest;
import software.amazon.awssdk.services.entityresolution.model.InternalServerException;
import software.amazon.awssdk.services.entityresolution.model.ResourceNotFoundException;
import software.amazon.awssdk.services.entityresolution.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@NoArgsConstructor
public class CreateHandler extends BaseHandler<CallbackContext> {

    public static final String SCHEMA_ALREADY_EXISTS_ERROR_MESSAGE = "SchemaMapping already exists";

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

        final CreateSchemaMappingRequest createSchemaMappingRequest = CreateSchemaMappingRequest.builder()
                                                                                                .description(
                                                                                                    requestModel.getDescription())
                                                                                                .mappedInputFields(
                                                                                                    Translator.translateFromInternalSchemaInputAttributes(
                                                                                                        requestModel.getMappedInputFields()))
                                                                                                .schemaName(
                                                                                                    requestModel.getSchemaName())
                                                                                                .tags(resourceTags)
                                                                                                .build();

        final CreateSchemaMappingResponse createSchemaMappingResponse;

        try {
            checkIfSchemaExists(proxy, requestModel);

            createSchemaMappingResponse = proxy.injectCredentialsAndInvokeV2(createSchemaMappingRequest,
                client::createSchemaMapping);

            logger.log(String.format("Created SchemaMapping with schemaName = %s", requestModel.getSchemaName()));
        } catch (final ConflictException e) {
            if (e.getMessage() != null && e.getMessage()
                                           .contains(SCHEMA_ALREADY_EXISTS_ERROR_MESSAGE)) {
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
                                                         .description(createSchemaMappingResponse.description())
                                                         .mappedInputFields(
                                                             Translator.translateToInternalSchemaInputAttributes(
                                                                 createSchemaMappingResponse.mappedInputFields()))
                                                         .schemaArn(createSchemaMappingResponse.schemaArn())
                                                         .schemaName(createSchemaMappingResponse.schemaName())
                                                         .build();

        return ProgressEvent.defaultSuccessHandler(responseModel);
    }

    /**
     * This helper function is used in order to pass Contract Test - test_create_create.
     *
     * To honor handler contract, if a resource is successfully created, a subsequent CREATE request with same
     * parameters must throw "AlreadyExists" exception.
     */
    private void checkIfSchemaExists(
        final AmazonWebServicesClientProxy proxy,
        final ResourceModel requestModel) {

        final GetSchemaMappingRequest getSchemaMappingRequest = GetSchemaMappingRequest.builder()
                                                                                       .schemaName(
                                                                                           requestModel
                                                                                               .getSchemaName())
                                                                                       .build();

        try {
            proxy.injectCredentialsAndInvokeV2(getSchemaMappingRequest, client::getSchemaMapping);
        } catch (final Exception e) {
            return;
        }
        throw ConflictException.builder()
                               .message(SCHEMA_ALREADY_EXISTS_ERROR_MESSAGE)
                               .build();
    }
}
