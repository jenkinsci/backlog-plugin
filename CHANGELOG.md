# CHANGELOG

## Version 2.6 (Mar 13, 2020)

-   Move docs to GitHub

## Version 2.5 (Mar 11, 2020)

-   Mask credentials transmitted in plain text ( [SECURITY-1510](https://jenkins.io/security/advisory/2020-03-09/#SECURITY-1510) )

## Version 2.4 (Oct 7, 2018)

-   Scan max count of pull requests ( default 20 → 100 )

## Version 2.3 (Aug 2, 2018)

-   Add Null Check for Backlog URL ( [ JENKINS-49396](https://issues.jenkins-ci.org/browse/JENKINS-49396) )

## Version 2.2 (May 31, 2018)

-   Avoid unneeded serialization
    ( [JEP-200](https://jenkins.io/blog/2018/01/13/jep-200/) )
-   Suppress error log related with a repository browser in a config
    page
-   Replace Backlog API v1(xml-rpc) to v2 ( see [this
    blog](https://backlog.com/ja/blog/backlog-old-api-2018-05-15/) )

    Input your API key instead of user / pass when creating issue on
    Backlog

## Version 2.1 (Jun 20, 2017)

-   Add a link to Backlog for pipeline
-   Update emoji ( see [this
    blog](https://nulab-inc.com/blog/backlog/backlog-gets-fresh-updated-emojis/)
    )

## Version 2.0 (May 28, 2017)

-   Support pipeline
-   Support multibranch pipeline for Backlog pull request.

## Version 1.11 (Oct 04, 2015)

-   Notify build result to Backlog pull request.

## Version 1.10 (May 06, 2014)

-   Allow '@' and '.' in the UserId field.

## Version 1.9 (Jan 12, 2013)

-   pull [\#1](https://github.com/jenkinsci/backlog-plugin/pull/1) : Fix
    the issue key pattern (thanks @emanon001).
-   Replace Backlog icon.
-   Store a encrypted value for password.

    If "Createing Backlog Issue" and/or "Publishing artifacts to Backlog
    File" becomes failed after version up, re-enter the password.

## Version 1.8 (Sep 06, 2012)

-   Add repository browser for Git.

## Version 1.7 (Mar 09, 2012)

-   Add publishing built artifacts to Backlog File.

## Version 1.6 (Oct 10, 2011)

-   Add selecting other project when specifying repository browser

## Version 1.5 (Aug 17, 2011)

-   Add security realm

## Version 1.4 (May 31, 2011)

-   Add link to SCM repository browser
-   Arrange properties like UserId/Password (Sorry for being
    incompatible previous version, so properties are initialized. Please
    retry setting.)

## Version 1.3 (Mar 14, 2011)

-   Add validation on "Create Issue on Backlog" fields

## Version 1.2 (Jan 09, 2011)

-   Create Backlog issue on error build
-   Enabled input project url
-   Localize Japanese

## Version 1.1 (Jan 28, 2010)

-   Update code for more recent Hudson

## Version 1.0 (May 26, 2009)

-   First release


