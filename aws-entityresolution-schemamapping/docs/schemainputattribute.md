# AWS::EntityResolution::SchemaMapping SchemaInputAttribute

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#fieldname" title="FieldName">FieldName</a>" : <i>String</i>,
    "<a href="#type" title="Type">Type</a>" : <i>String</i>,
    "<a href="#subtype" title="SubType">SubType</a>" : <i>String</i>,
    "<a href="#groupname" title="GroupName">GroupName</a>" : <i>String</i>,
    "<a href="#matchkey" title="MatchKey">MatchKey</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#fieldname" title="FieldName">FieldName</a>: <i>String</i>
<a href="#type" title="Type">Type</a>: <i>String</i>
<a href="#subtype" title="SubType">SubType</a>: <i>String</i>
<a href="#groupname" title="GroupName">GroupName</a>: <i>String</i>
<a href="#matchkey" title="MatchKey">MatchKey</a>: <i>String</i>
</pre>

## Properties

#### FieldName

_Required_: Yes

_Type_: String

_Maximum Length_: <code>255</code>

_Pattern_: <code>^[a-zA-Z_0-9- \t]*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Type

_Required_: Yes

_Type_: String

_Allowed Values_: <code>NAME</code> | <code>NAME_FIRST</code> | <code>NAME_MIDDLE</code> | <code>NAME_LAST</code> | <code>ADDRESS</code> | <code>ADDRESS_STREET1</code> | <code>ADDRESS_STREET2</code> | <code>ADDRESS_STREET3</code> | <code>ADDRESS_CITY</code> | <code>ADDRESS_STATE</code> | <code>ADDRESS_COUNTRY</code> | <code>ADDRESS_POSTALCODE</code> | <code>PHONE</code> | <code>PHONE_NUMBER</code> | <code>PHONE_COUNTRYCODE</code> | <code>EMAIL_ADDRESS</code> | <code>UNIQUE_ID</code> | <code>DATE</code> | <code>STRING</code> | <code>PROVIDER_ID</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SubType

The subtype of the Attribute. Would be required only when type is PROVIDER_ID

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### GroupName

_Required_: No

_Type_: String

_Maximum Length_: <code>255</code>

_Pattern_: <code>^[a-zA-Z_0-9- \t]*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MatchKey

_Required_: No

_Type_: String

_Maximum Length_: <code>255</code>

_Pattern_: <code>^[a-zA-Z_0-9- \t]*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
