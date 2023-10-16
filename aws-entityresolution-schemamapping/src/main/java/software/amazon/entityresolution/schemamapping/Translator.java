package software.amazon.entityresolution.schemamapping;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.entityresolution.model.SchemaInputAttribute;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

/**
 * This class is a centralized placeholder for - api request construction - object translation to/from aws sdk -
 * resource model construction for read/list handlers
 */
public class Translator {

    public static String ARN_FORMAT = "arn:%s:entityresolution:%s:%s:schemamapping/%s";

    public static List<SchemaInputAttribute> translateFromInternalSchemaInputAttributes(
        List<software.amazon.entityresolution.schemamapping.SchemaInputAttribute> source) {

        if (source == null) {
            return null;
        }

        return source.stream()
                     .map(schemaInputAttribute -> SchemaInputAttribute.builder()
                                                                      .fieldName(
                                                                          schemaInputAttribute.getFieldName())
                                                                      .type(
                                                                          schemaInputAttribute.getType())
                                                                      .subType(
                                                                          schemaInputAttribute.getSubType())
                                                                      .groupName(
                                                                          schemaInputAttribute.getGroupName())
                                                                      .matchKey(
                                                                          schemaInputAttribute.getMatchKey())
                                                                      .build())
                     .collect(Collectors.toList());
    }

    public static List<software.amazon.entityresolution.schemamapping.SchemaInputAttribute> translateToInternalSchemaInputAttributes(
        List<SchemaInputAttribute> source) {

        if (source == null) {
            return null;
        }

        return source.stream()
                     .map(
                         schemaInputAttribute -> software.amazon.entityresolution.schemamapping.SchemaInputAttribute.builder()
                                                                                                                    .fieldName(
                                                                                                                        schemaInputAttribute.fieldName())
                                                                                                                    .type(
                                                                                                                        String.valueOf(
                                                                                                                            schemaInputAttribute.type()))
                                                                                                                    .subType(
                                                                                                                        schemaInputAttribute.subType())
                                                                                                                    .groupName(
                                                                                                                        schemaInputAttribute.groupName())
                                                                                                                    .matchKey(
                                                                                                                        schemaInputAttribute.matchKey())
                                                                                                                    .build())
                     .collect(Collectors.toList());
    }

    public static Set<Tag> mapTagsToSet(Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }

        return tags.entrySet()
                   .stream()
                   .map(t -> Tag.builder()
                                .key(t.getKey())
                                .value(t.getValue())
                                .build())
                   .collect(Collectors.toSet());
    }

    public static String toSchemaArn(final ResourceHandlerRequest<ResourceModel> request) {
        return String.format(ARN_FORMAT, request.getAwsPartition(), request.getRegion(), request.getAwsAccountId(),
            request.getDesiredResourceState()
                   .getSchemaName());
    }
}
