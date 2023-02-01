[1mdiff --git a/ontimize-jee-server-keycloak/src/main/java/com/ontimize/jee/server/security/keycloak/OntimizeKeycloakUserDetailsAuthenticationProvider.java b/ontimize-jee-server-keycloak/src/main/java/com/ontimize/jee/server/security/keycloak/OntimizeKeycloakUserDetailsAuthenticationProvider.java[m
[1mindex c9e95f0..75a0aa9 100644[m
[1m--- a/ontimize-jee-server-keycloak/src/main/java/com/ontimize/jee/server/security/keycloak/OntimizeKeycloakUserDetailsAuthenticationProvider.java[m
[1m+++ b/ontimize-jee-server-keycloak/src/main/java/com/ontimize/jee/server/security/keycloak/OntimizeKeycloakUserDetailsAuthenticationProvider.java[m
[36m@@ -18,7 +18,6 @@[m [mimport org.springframework.security.core.Authentication;[m
 import org.springframework.security.core.AuthenticationException;[m
 import org.springframework.security.core.GrantedAuthority;[m
 import org.springframework.security.core.authority.SimpleGrantedAuthority;[m
[31m-import org.springframework.security.core.userdetails.UsernameNotFoundException;[m
 [m
 import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;[m
 import com.ontimize.jee.common.security.XMLClientUtilities;[m
[36m@@ -47,7 +46,7 @@[m [mpublic class OntimizeKeycloakUserDetailsAuthenticationProvider extends KeycloakA[m
 		final String username = accessToken.getPreferredUsername();[m
 [m
 		if (username == null) {[m
[31m-			throw new UsernameNotFoundException("username was null");[m
[32m+[m			[32mthrow new OntimizeJEERuntimeException("username was null");[m
 		}[m
 [m
 		final List<GrantedAuthority> authorities = new ArrayList<>();[m
[1mdiff --git a/ontimize-jee-server-keycloak/src/main/java/com/ontimize/jee/server/security/keycloak/admin/UserManagementKeycloakImpl.java b/ontimize-jee-server-keycloak/src/main/java/com/ontimize/jee/server/security/keycloak/admin/UserManagementKeycloakImpl.java[m
[1mindex 53620d1..1703c75 100644[m
[1m--- a/ontimize-jee-server-keycloak/src/main/java/com/ontimize/jee/server/security/keycloak/admin/UserManagementKeycloakImpl.java[m
[1m+++ b/ontimize-jee-server-keycloak/src/main/java/com/ontimize/jee/server/security/keycloak/admin/UserManagementKeycloakImpl.java[m
[36m@@ -640,7 +640,7 @@[m [mpublic class UserManagementKeycloakImpl implements IUserManagement {[m
 		// for users in realm, assign roles for those specified[m
 		for (UserRepresentation userRep : userR) {[m
 			final Optional<UserRoles> ur = userRoles.stream()[m
[31m-					.filter(uroles -> uroles.getUser_().equalsIgnoreCase(userRep.getEmail())).findFirst();[m
[32m+[m					[32m.filter(uroles -> uroles.getUser().equalsIgnoreCase(userRep.getEmail())).findFirst();[m
 [m
 			if (ur.isPresent()) {[m
 				final List<String> roles = Arrays.asList(ur.get().getAssignedRoles());[m
[1mdiff --git a/ontimize-jee-server-keycloak/src/main/java/com/ontimize/jee/server/security/keycloak/admin/dto/application/UserRoles.java b/ontimize-jee-server-keycloak/src/main/java/com/ontimize/jee/server/security/keycloak/admin/dto/application/UserRoles.java[m
[1mindex 1be460b..725b322 100644[m
[1m--- a/ontimize-jee-server-keycloak/src/main/java/com/ontimize/jee/server/security/keycloak/admin/dto/application/UserRoles.java[m
[1m+++ b/ontimize-jee-server-keycloak/src/main/java/com/ontimize/jee/server/security/keycloak/admin/dto/application/UserRoles.java[m
[36m@@ -2,15 +2,15 @@[m [mpackage com.ontimize.jee.server.security.keycloak.admin.dto.application;[m
 [m
 public class UserRoles {[m
 [m
[31m-	private String user_;[m
[32m+[m	[32mprivate String user;[m
 	private String[] assignedRoles;[m
 [m
[31m-	public String getUser_() {[m
[31m-		return user_;[m
[32m+[m	[32mpublic String getUser() {[m
[32m+[m		[32mreturn user;[m
 	}[m
 [m
[31m-	public void setUser_(String user_) {[m
[31m-		this.user_ = user_;[m
[32m+[m	[32mpublic void setUser(String user) {[m
[32m+[m		[32mthis.user = user;[m
 	}[m
 [m
 	public String[] getAssignedRoles() {[m
