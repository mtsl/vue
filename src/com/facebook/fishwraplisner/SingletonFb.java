package com.facebook.fishwraplisner;
public class SingletonFb {
	public NotifiShowAlert notifyInstance;
	  private static SingletonFb singleInstance = new SingletonFb();
	  private SingletonFb() {}
	  public static SingletonFb getSingleInstance() {
	    return singleInstance;
	  }
	  public void setNotifyAlerInstance(NotifiShowAlert notifyInstance) {
		  this.notifyInstance = notifyInstance;
	}
	  public NotifiShowAlert getNotifyAlertInstance(){
			return notifyInstance;
			
		}
	}