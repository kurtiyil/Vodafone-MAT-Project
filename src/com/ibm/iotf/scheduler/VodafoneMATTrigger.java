package com.ibm.iotf.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import deviceSimulation.VodafoneMAT;

public class VodafoneMATTrigger implements Job {

	    public void execute(JobExecutionContext arg0) throws JobExecutionException {
	        // TODO Auto-generated method stub
	        try {
	            System.out.println("Job started : " + arg0.getFireTime().toString());
	            VodafoneMAT alarmJob = new VodafoneMAT();
	            alarmJob.doJob();
	        } catch (Exception ex) {
	            System.out.println(ex.getMessage());
	        }
	    }
	
}
