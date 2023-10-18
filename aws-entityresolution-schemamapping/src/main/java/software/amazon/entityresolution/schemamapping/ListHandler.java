package software.amazon.entityresolution.schemamapping;

import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.entityresolution.EntityResolutionClient;
import software.amazon.awssdk.services.entityresolution.model.AccessDeniedException;
import software.amazon.awssdk.services.entityresolution.model.InternalServerException;
import software.amazon.awssdk.services.entityresolution.model.ListSchemaMappingsRequest;
import software.amazon.awssdk.services.entityresolution.model.ListSchemaMappingsResponse;
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
public class ListHandler extends BaseHandler<CallbackContext> {

    private EntityResolutionClient client;

    public ListHandler(EntityResolutionClient client) {
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

        final ListSchemaMappingsRequest listSchemaMappingsRequest = ListSchemaMappingsRequest.builder()
                                                                                             .nextToken(
                                                                                                 request.getNextToken())
                                                                                             .build();
        final ListSchemaMappingsResponse listSchemaMappingsResponse;

        try {
            listSchemaMappingsResponse = proxy.injectCredentialsAndInvokeV2(listSchemaMappingsRequest,
                client::listSchemaMappings);
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

        List<ResourceModel> responseModels = new ArrayList<>();

        listSchemaMappingsResponse.schemaList()
                                  .forEach(schema -> {
                                      ResourceModel responseModel = ResourceModel.builder()
                                                                                 .createdAt(schema.createdAt()
                                                                                                  .toString())
                                                                                 .schemaArn(schema.schemaArn())
                                                                                 .schemaName(schema.schemaName())
                                                                                 .updatedAt(schema.updatedAt()
                                                                                                  .toString())
                                                                                 .hasWorkflows(schema.hasWorkflows())
                                                                                 .build();
                                      responseModels.add(responseModel);
                                  });

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .resourceModels(responseModels)
                            .nextToken(listSchemaMappingsResponse.nextToken())
                            .status(OperationStatus.SUCCESS)
                            .build();
    }
}
