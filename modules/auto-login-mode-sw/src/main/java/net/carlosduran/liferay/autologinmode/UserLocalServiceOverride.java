package net.carlosduran.liferay.autologinmode;

import com.liferay.portal.kernel.service.UserLocalServiceWrapper;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;

import java.util.Map;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.auth.Authenticator;
import com.liferay.portal.kernel.service.ServiceWrapper;

import org.osgi.service.component.annotations.Component;

/**
 * @author Carlos
 */
@Component(
	immediate = true,
	property = {
	},
	service = ServiceWrapper.class
)
public class UserLocalServiceOverride extends UserLocalServiceWrapper {

	public UserLocalServiceOverride() {
		super(null);
	}

	@Override
	public int authenticateByEmailAddress(long companyId, String emailAddress, String password,
			Map<String, String[]> headerMap, Map<String, String[]> parameterMap, Map<String, Object> resultsMap)
			throws PortalException {
		if(isAutoModeEnabled()) {
			return autoSelectAuthentication(companyId, emailAddress, password, headerMap, parameterMap, resultsMap);
		} else {
			return super.authenticateByEmailAddress(companyId, emailAddress, password, headerMap, parameterMap, resultsMap);
		}
	}

	@Override
	public int authenticateByScreenName(long companyId, String screenName, String password,
			Map<String, String[]> headerMap, Map<String, String[]> parameterMap, Map<String, Object> resultsMap)
			throws PortalException {
		if(isAutoModeEnabled()) {
			return autoSelectAuthentication(companyId, screenName, password, headerMap, parameterMap, resultsMap);
		} else {
			return super.authenticateByScreenName(companyId, screenName, password, headerMap, parameterMap, resultsMap);
		}
	}

	@Override
	public int authenticateByUserId(long companyId, long userId, String password, Map<String, String[]> headerMap,
			Map<String, String[]> parameterMap, Map<String, Object> resultsMap) throws PortalException {
		if(isAutoModeEnabled()) {
			return autoSelectAuthentication(companyId, String.valueOf(userId), password, headerMap, parameterMap, resultsMap);
		} else {
			return super.authenticateByUserId(companyId, userId, password, headerMap, parameterMap, resultsMap);
		}
	}
	
	private int autoSelectAuthentication(long companyId, String authenticationKey, String password,
			Map<String, String[]> headerMap, Map<String, String[]> parameterMap, Map<String, Object> resultsMap)
	throws PortalException {
		
		int authenticationResult = Authenticator.FAILURE;
		
		try {
			authenticationResult = super.authenticateByUserId(
					companyId, Long.parseLong(authenticationKey), password, headerMap, parameterMap, resultsMap);
			
		} catch (Exception e) {}
		
		if(authenticationResult != Authenticator.SUCCESS) {
			authenticationResult = super.authenticateByScreenName(
					companyId, authenticationKey, password, headerMap, parameterMap, resultsMap);
		}
		
		if(authenticationResult != Authenticator.SUCCESS && authenticationKey.contains(StringPool.AT)) {
			authenticationResult = super.authenticateByEmailAddress(
					companyId, authenticationKey, password, headerMap, parameterMap, resultsMap);
		}
		
		return authenticationResult;
		
	}
	
	private boolean isAutoModeEnabled() {
		String autoModeProp = GetterUtil.getString(PropsUtil.get("auto-login-mode"), StringPool.BLANK);
		if(autoModeProp.equalsIgnoreCase("enabled")) {
			return true;
		}		
		return false;
		
	}

}