package com.castrovala.fraser.orbwar.damage;

import com.castrovala.fraser.orbwar.gameobject.GameObject;

public class DamageInfo {
	private GameObject damager;
	private DamageCause cause;
	
	public DamageInfo(GameObject damager, DamageCause cause) {
		this.damager = damager;
		this.cause = cause;
	}

	public GameObject getDamager() {
		return damager;
	}

	public void setDamager(GameObject damager) {
		this.damager = damager;
	}

	public DamageCause getCause() {
		return cause;
	}

	public void setCause(DamageCause cause) {
		this.cause = cause;
	}

}
