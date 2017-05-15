package com.little.visit;


import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

public class OKHttpManager {
	public List<TaskModel> taskList = new ArrayList<TaskModel>();

	private static OKHttpManager okHttpManager;

	private OKHttpManager() {
	}

	public static synchronized OKHttpManager getOkHttpManager() {
		if (okHttpManager == null) {
			okHttpManager = new OKHttpManager();
		}
		return okHttpManager;
	}

	/**
	 * 添加线程
	 */
	public void addTask(String tagString, Call task){
		try {
			TaskModel taskModel = new TaskModel();
			taskModel.tagString = tagString;
			taskModel.task = task;
			taskModel.creatTime = System.currentTimeMillis();
			taskList.add(taskModel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭所有还在运行的线程
	 */
	public void cancelAllTasks(){
		for(int i=0;i<taskList.size();i++){
			try {
				TaskModel taskModel = taskList.get(i);
				if(taskModel.task!=null){
					taskModel.task.cancel();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		taskList.clear();
	}

	/**
	 * 关闭tagString所标识的Activity或者Fragment中所有还在运行的线程
	 * @param tagString
	 */
	public void cancelTasksByTag(String tagString){
		List<TaskModel> taskModels = new ArrayList<TaskModel>();
		for(int i=0;i<taskList.size();i++){
			try {
				TaskModel taskModel = taskList.get(i);
				if(taskModel.tagString.equals(tagString)&&taskModel.task!=null){
					long time = System.currentTimeMillis()-taskModel.getCreatTime();
					if (time>500){
						taskModel.task.cancel();
						taskModels.add(taskModel);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for (int i=0;i<taskModels.size();i++){
			try {
				taskList.remove(taskModels.get(i));
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	/**
	 * 关闭一个还在运行的线程
	 */
	public void cancelTask(Call task){
		for(int i=0;i<taskList.size();i++){
			try {
				TaskModel taskModel = taskList.get(i);
				if(taskModel.task!=null&&taskModel.task==task){
					taskModel.task.cancel();
					taskList.remove(taskModel);
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 移除task
	 * @param task
	 */
	public void removeTask(Call task){
		for(int i=0;i<taskList.size();i++){
			try {
				TaskModel taskModel = taskList.get(i);
				if(taskModel.task!=null&&taskModel.task==task){
					taskList.remove(taskModel);
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	public class TaskModel{
		long creatTime;
		String tagString;
		Call task;

		public long getCreatTime() {
			return creatTime;
		}

		public void setCreatTime(long creatTime) {
			this.creatTime = creatTime;
		}

		public String getTagString() {
			return tagString;
		}

		public void setTagString(String tagString) {
			this.tagString = tagString;
		}

		public Call getTask() {
			return task;
		}

		public void setTask(Call task) {
			this.task = task;
		}
	}


}