package com.ibm.iotf.scheduler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
	
	@WebServlet("/AlarmJobListener")
	public class VodafoneMATListener extends HttpServlet implements ServletContextListener {
	    private static final long serialVersionUID = 1L;
	    private static final SchedulerFactory schedulerFactory = new StdSchedulerFactory();
	    private Scheduler scheduler = null;

	    public VodafoneMATListener() {
	        super();
	    }

	    public void contextInitialized(ServletContextEvent arg0) {
	        try {
	            System.out.println("quartz scheduler trying to start.");
	            JobDetail job = JobBuilder.newJob(VodafoneMATTrigger.class)
	                    .withIdentity("VodafoneMATTrigger", "AlarmJobTriggerGroup").build();
	            
	            
	            
	            Trigger trigger = TriggerBuilder.newTrigger()
	                    .withIdentity("VodafoneMATTriggerTrigger", "AlarmJobTriggerGroup")
	                    .withSchedule(CronScheduleBuilder.cronScheduleNonvalidatedExpression("0 0/1 * * * ?"))
	                    .build();
	            scheduler = schedulerFactory.getScheduler();
	            scheduler.start();
	            scheduler.scheduleJob(job, trigger);
	            System.out.println("quartz scheduler started.");
	        } catch (SchedulerException schEx) {
	            System.out.println(schEx.getMessage());
	        } catch (Exception ex) {
	            System.out.println(ex.getMessage());
	        }
	    }

	    public void contextDestroyed(ServletContextEvent arg0) {
	        try {
	            if (scheduler != null && scheduler.isStarted()) {
	                scheduler.shutdown();
	                System.out.println("quartz scheduler closed successfully.");
	            }
	        } catch (SchedulerException schEx) {
	            System.out.println(schEx.getMessage());
	        } catch (Exception exception) {
	            System.out.println(exception.getMessage());
	        }
	    }
	}
