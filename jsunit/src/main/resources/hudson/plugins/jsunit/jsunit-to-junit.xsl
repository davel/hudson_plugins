<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:lxslt="http://xml.apache.org/xslt"
                xmlns:redirect="http://xml.apache.org/xalan/redirect"
                xmlns:stringutils="xalan://org.apache.tools.ant.util.StringUtils"
                extension-element-prefixes="redirect">
    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
    <xsl:template match="browserResult">
        <xsl:variable name="browser" select="browser/displayName" />
        <testsuites>
            <xsl:variable name="numberOfTests" select="count(descendant::testCaseResult)" />
            <xsl:variable name="numberOfErrors" select="count(descendant::error)" />
            <xsl:variable name="numberOfFailures" select="count(descendant::failure)" />
            <xsl:variable name="hostName" select="properties/property[@name = 'remoteAddress']/@value" />
            <xsl:variable name="testSuiteName" select="properties/property[@name = 'testPage']/@value" />
            <xsl:variable name="totalTime" select='format-number(sum(descendant::testCaseResult/@time), "#.###")' />
            <testsuite errors="{$numberOfErrors}" failures="{$numberOfFailures}" hostname="{$hostName}" name="{$browser}.{$testSuiteName}" tests="{$numberOfTests}" time="{$totalTime}">
                <xsl:copy-of select="properties" /> <!-- copy all properties -->
                <xsl:for-each select="testCaseResults/testCaseResult">
                    <xsl:variable name="classname">
                        <xsl:call-template name="get-classname">
                            <xsl:with-param name="testname" select="substring-before(@name,'.html:')"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <testcase classname="JsUnit.{$browser}.{$classname}" name="{substring-after(@name,'.html:')}" time="{@time}">
                        <xsl:copy-of select="*" /> <!-- copy all failures and errors -->
                    </testcase>
                </xsl:for-each>
            </testsuite>
        </testsuites>
    </xsl:template>

    <!-- recursively strips slashes from the beginning until none are left -->
    <xsl:template name="get-classname">
        <xsl:param name="testname" />
        <xsl:choose>
            <xsl:when test="contains($testname, '/')">
                <xsl:call-template name="get-classname">
                    <xsl:with-param name="testname" select="substring-after($testname, '/')" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$testname" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>