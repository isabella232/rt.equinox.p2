package org.eclipse.equinox.internal.p2.ui.sdk;

/**
 * Help context ids for the P2 SDK
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * @since 3.4
 */

public interface IProvSDKHelpContextIds {
	public static final String PREFIX = ProvSDKUIActivator.PLUGIN_ID + "."; //$NON-NLS-1$

	public static final String UPDATE_AND_INSTALL_DIALOG = PREFIX + "update_and_install_dialog_context"; //$NON-NLS-1$ 

	public static final String REPOSITORY_MANIPULATION_DIALOG = PREFIX + "repository_manipulation_dialog_context"; //$NON-NLS-1$ 

	public static final String PROVISIONING_PREFERENCE_PAGE = PREFIX + "provisioning_preference_page_context"; //$NON-NLS-1$

	public static final String AUTOMATIC_UPDATES_PREFERENCE_PAGE = PREFIX + "automatic_updates_preference_page_context"; //$NON-NLS-1$

	public static final String EXTERNAL_FILES_PREFERENCE_PAGE = PREFIX + "external_files_preference_page_context"; //$NON-NLS-1$

}