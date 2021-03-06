package org.zhadok.poe.controller.config.pojo;

public class ConfigCharacterMovement {

	/**
	 * How many pixels (x axis) should the mouse be offset for character movement?
	 * This should be the distance from the middle of the screen to the middle of the character
	 */
	private int mouseOffsetCharacterToScreenCenterX;
	/**
	 * How many pixels (y axis) should the mouse be offset for character movement?
	 * This should be the distance from the middle of the screen to the middle of the character
	 */
	private int mouseOffsetCharacterToScreenCenterY;

	private double mouseDistance_ScreenSizeMultiplier;

	private double stickThreshold = 0.1;

	private ConfigAction[] actionsOnStickRelease;

	public int getMouseOffsetCharacterToScreenCenterX() {
		return 1080;
		// return mouseOffsetCharacterToScreenCenterX;
	}

	public int getMouseOffsetCharacterToScreenCenterY() {
		return mouseOffsetCharacterToScreenCenterY;
	}

	public void setMouseOffsetCharacterToScreenCenterY(int number) {
		this.mouseOffsetCharacterToScreenCenterY = number;
	}

	public double getMouseDistance_ScreenSizeMultiplier() {
		return mouseDistance_ScreenSizeMultiplier;
	}

	public void setMouseDistance_ScreenSizeMultiplier(double number) {
		this.mouseDistance_ScreenSizeMultiplier = number;
	}

	public double getStickThreshold() {
		return stickThreshold;
	}

	public ConfigAction[] getActionsOnStickRelease() {
		return actionsOnStickRelease;
	}

	@Override
	public String toString() {
		return "ConfigCharacterMovement [mouseOffsetCharacterToScreenCenterX=" + mouseOffsetCharacterToScreenCenterX
				+ ", mouseOffsetCharacterToScreenCenterY=" + mouseOffsetCharacterToScreenCenterY
				+ ", mouseDistance_ScreenSizeMultiplier=" + mouseDistance_ScreenSizeMultiplier + ", stickThreshold="
				+ stickThreshold + "]";
	}



}
