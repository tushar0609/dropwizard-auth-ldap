LDAP Authenticator 
==================

This is a simple dropwizard-auth module using Basic-Auth + LDAP for authentication. 

Maven
-----
   
```xml
<dependency>
    <groupId>com.yammer.dropwizard</groupId>
    <artifactId>dropwizard-auth-ldap</artifactId>
    <version>2.0.34-2</version>
</dependency>
```

Set Configuration
----------

```java
LdapConfiguration configuration = new LdapConfiguration();
LdapAuthenticator authenticator = new LdapAuthenticator(configuration);
authenticator.authenticate(new BasicCredentials("user", "password"));
```

Add it to your Service
----------------------
I assume you are already familiar with dropwizard's authentication module.
You can find more information about dropwizard authentication at http://www.dropwizard.io/manual/auth.html

Here is an example how to add `LdapAuthenticator` using a `CachingAuthenticator` to your service:

```java
@Override
public void run(ValhallaServiceConfiguration configuration, Environment environment) throws Exception{
        LdapConfiguration ldapConfiguration=configuration.getLdapConfiguration();
        CachingAuthenticator ldapAuthenticator=new CachingAuthenticator(environment.metrics(),
        new UserResourceAuthenticator(new LdapAuthenticator(ldapConfiguration)),
        ldapConfiguration.getCachePolicy());

        environment.jersey().register(new LdapAuthDynamicFeature(
        new BasicCredentialAuthFilter.Builder<LdapUser>()
        .setAuthenticator(ldapAuthenticator)
        .setAuthorizer((Authorizer<LdapUser>)(user,role)->user.getRoles().contains(role))
        .setRealm("realm")
        .buildAuthFilter()));

        environment.jersey().register(LdapRolesAllowedDynamicFeature.class);
        //If you want to use @Auth to inject a custom Principal type into your resource
        environment.jersey().register(new LdapAuthValueFactoryProvider.Binder<>(LdapUser.class));
}
```

Additional Notes
----------------------

Example Resource:

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

Register the resource:
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