# AWS::EntityResolution::IdMappingWorkflow ProviderProperties

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#providerservicearn" title="ProviderServiceArn">ProviderServiceArn</a>" : <i>String</i>,
    "<a href="#providerconfiguration" title="ProviderConfiguration">ProviderConfiguration</a>" : <i><a href="providerproperties-providerconfiguration.md">ProviderConfiguration</a></i>,
    "<a href="#intermediatesourceconfiguration" title="IntermediateSourceConfiguration">IntermediateSourceConfiguration</a>" : <i><a href="intermediatesourceconfiguration.md">IntermediateSourceConfiguration</a></i>
}
</pre>

### YAML

<pre>
<a href="#providerservicearn" title="ProviderServiceArn">ProviderServiceArn</a>: <i>String</i>
<a href="#providerconfiguration" title="ProviderConfiguration">ProviderConfiguration</a>: <i><a href="providerproperties-providerconfiguration.md">ProviderConfiguration</a></i>
<a href="#intermediatesourceconfiguration" title="IntermediateSourceConfiguration">IntermediateSourceConfiguration</a>: <i><a href="intermediatesourceconfiguration.md">IntermediateSourceConfiguration</a></i>
</pre>

## Properties

#### ProviderServiceArn

Arn of the Provider Service being used.

_Required_: Yes

_Type_: String

_Pattern_: <code>^arn:(aws|aws-us-gov|aws-cn):entityresolution:([A-Za-z0-9]+(-[A-Za-z0-9]+)+)::providerservice/[A-Za-z0-9]+/[A-Za-z0-9]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ProviderConfiguration

Additional Provider configuration that would be required for the provider service. The Configuration must be in JSON string format

_Required_: No

_Type_: <a href="providerproperties-providerconfiguration.md">ProviderConfiguration</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### IntermediateSourceConfiguration

_Required_: No

_Type_: <a href="intermediatesourceconfiguration.md">IntermediateSourceConfiguration</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
