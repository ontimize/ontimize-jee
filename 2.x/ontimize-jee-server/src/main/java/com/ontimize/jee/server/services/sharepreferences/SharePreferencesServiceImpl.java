package com.ontimize.jee.server.services.sharepreferences;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ontimize.db.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.security.PermissionsProviderSecured;
import com.ontimize.jee.common.services.formprovider.IFormProviderService;
import com.ontimize.jee.common.services.sharepreferences.ISharePreferencesService;
import com.ontimize.jee.server.configuration.OntimizeConfiguration;
import com.ontimize.util.share.SharedElement;

@Service("SharePreferencesService")
public class SharePreferencesServiceImpl implements ISharePreferencesService, ApplicationContextAware {

	/** The engine. */
	private ISharePreferencesService engine;

	/**
	 * The Constructor.
	 */
	public SharePreferencesServiceImpl() {
		super();
	}

	/**
	 * Gets the {@link IFormProviderService} engine
	 *
	 * @return the {@link IFormProviderService} engine
	 */
	public ISharePreferencesService getEngine() {
		return this.engine;
	}

	/**
	 * Sets the engine
	 *
	 * @param engine
	 *            {@link ISharePreferencesService} the engine
	 */
	public void setEngine(ISharePreferencesService engine) {
		this.engine = engine;
	}

	/**
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.setEngine(applicationContext.getBean(OntimizeConfiguration.class).getSharePreferencesConfiguration().getEngine());
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public List<String> getUserList() throws OntimizeJEEException {
		return this.getEngine().getUserList();
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public EntityResult addSharedItem(SharedElement sharedObject, List<String> targetList) throws OntimizeJEEException {
		return this.getEngine().addSharedItem(sharedObject, targetList);
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public EntityResult editTargetSharedElement(int idShare, List<String> targetList) throws OntimizeJEEException {
		return this.getEngine().editTargetSharedElement(idShare, targetList);
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public List<SharedElement> getSharedItemsWithUser(String username) throws OntimizeJEEException {
		return this.getEngine().getSharedItemsWithUser(username);
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public List<SharedElement> getSharedItemsWithUserAndKey(String username, String shareKey) throws OntimizeJEEException {
		return this.getEngine().getSharedItemsWithUserAndKey(username, shareKey);
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public List<HashMap<String, Object>> getTargetSharedElementMenuList(String username, String shareKey) throws OntimizeJEEException {
		return this.getEngine().getTargetSharedElementMenuList(username, shareKey);
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public List<String> getTargetSharedItemsList(int idShare) throws OntimizeJEEException {
		return this.getEngine().getTargetSharedItemsList(idShare);
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public List<SharedElement> getSourceSharedItemsList(String username, String shareKey) throws OntimizeJEEException {
		return this.getEngine().getSourceSharedItemsList(username, shareKey);
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public List<HashMap<String, Object>> getSourceSharedElementMenuList(String username, String shareKey) throws OntimizeJEEException {
		return this.getEngine().getSourceSharedElementMenuList(username, shareKey);
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public String getSharedElementMessage(int idShare) throws OntimizeJEEException {
		return this.getEngine().getSharedElementMessage(idShare);
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Object getSharedElementValue(int idShare) throws OntimizeJEEException {
		return this.getEngine().getSharedElementValue(idShare);
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public int countSharedItemByNameAndUser(String shareName) throws OntimizeJEEException {
		return this.getEngine().countSharedItemByNameAndUser(shareName);
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public SharedElement getSharedItem(int idShare) throws OntimizeJEEException {
		return this.getEngine().getSharedItem(idShare);
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public SharedElement getTargetSharedItem(int idShareTarget) throws OntimizeJEEException {
		return this.getEngine().getTargetSharedItem(idShareTarget);
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public EntityResult deleteSharedItem(int idShare) throws OntimizeJEEException {
		return this.getEngine().deleteSharedItem(idShare);
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public EntityResult deleteTargetSharedItem(int idShareTarget) throws OntimizeJEEException {
		return this.getEngine().deleteTargetSharedItem(idShareTarget);
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public EntityResult updateSharedItem(int idShare, String content, String message, String name) throws OntimizeJEEException {
		return this.getEngine().updateSharedItem(idShare, content, message, name);
	}


	// @Secured({ PermissionsProviderSecured.SECURED })
	// @Transactional(rollbackFor = Exception.class)
	// @Override
	// public String getXMLForm(String form) throws Exception {
	// return this.getEngine().getXMLForm(form);
	// }

}
