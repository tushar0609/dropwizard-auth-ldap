LDAP Authenticator
==================

This is a simple dropwizard-auth module using Basic-Auth + LDAP for authentication.

Maven
-----

```xml
<dependency>
    <groupId>com.yammer.dropwizard</groupId>
    <artifactId>dropwizard-auth-ldap</artifactId>
    <version>2.1.3-1</version>
</dependency>
```

Initialize the bundle
----------------------
I assume you are already familiar with dropwizard's authentication module.
You can find more information about dropwizard authentication at http://www.dropwizard.io/manual/auth.html

Here is an example how to use `LdapAuthenticatorBundle`:

```java
@Override
public void bootstrap(final Bootstrap<ServiceConfiguration> bootstrap)throws Exception{
        LdapAuthenticatorBundle ldapBundle=new LdapAuthenticatorBundle<ServiceConfiguration>(){

public LdapConfiguration getCOnfiguration(ServiceConfiguration configuration){
        configuration.getLdapConfiguration()
        }
        }
        }
```

Define resource with Ldap authentication annotations
----------------------

```java
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
@Singleton
@Api(value = "Login")
@AllArgsConstructor
@NoArgsConstructor
public class LoginResource {

    @POST
    @Path("/login")
    @RolesAllowed({"USER"})
    public Response login(@Auth User user) {
        return Response.ok().build();
    }
}
```

Register the resource
-----------

```java
environment.jersey().register(new LoginResource());
```

Configuration
-------------

```yml
uri: ldaps://myldap.com:636
cachePolicy: maximumSize=10000, expireAfterWrite=10m
userFilter: ou=people,dc=yourcompany,dc=com
groupFilter: ou=groups,dc=yourcompany,dc=com
userNameAttribute: cn
groupNameAttribute: cn
groupMembershipAttribute: memberUid
groupClassName: posixGroup
restrictToGroups:
    - user
    - admin
    - bots
connectTimeout: 500ms
readTimeout: 500ms
```