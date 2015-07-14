package com.cyanspring.common.account;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.cyanspring.common.business.GroupManagement;
import com.cyanspring.common.message.ErrorMessage;

public class UserGroupTest {
	
	private String rmNames[] = {
								 "rm1","rm2","rm3"
								,"rm4","rm5","rm6"
								,"rm7","rm8","rm9"
								,"rm10","rm11","rm12","rm13"
								,"rm14","rm15","rm16"
								,"rm17","rm18","rm19"
								,"rm20"
								};
	
	private String traderNames[] = {
									 "trader1","trader2","trader3"
									,"trader4","trader5"
									,"trader6","trader7","trader8"
									,"trader9","trader10","trder11"
									,"trader12","trader13","trder14"
									,"trader15","trader16","trder17"
									,"trader18","trader19","trder20"
									};
	
	private User rms[] ;
	private User traders[] ;
	private UserGroup rmGroups[];
	private UserGroup traderGroups[];
	
	private ConcurrentHashMap<String,UserGroup> userGroups = new ConcurrentHashMap<String,UserGroup>();

	@Before
	public void setUp() throws Exception {
		
		rms = new User[rmNames.length];
		traders = new User[traderNames.length];
		rmGroups = new UserGroup[rmNames.length];
		traderGroups = new UserGroup[traderNames.length];
		
		for(int i =0 ; i < rms.length ; i++){
			rms[i] = new User(rmNames[i],rmNames[i]);
			rms[i].setRole(UserRole.RiskManager);
			rmGroups[i] = new UserGroup(rmNames[i],rms[i].getRole());
			userGroups.put(rmNames[i], rmGroups[i]);
		}
		
		for(int i =0 ; i < traders.length ; i++){
			traders[i] = new User(traderNames[i],traderNames[i]);
			traders[i].setRole(UserRole.Trader);
			traderGroups[i] = new UserGroup(traderNames[i],traders[i].getRole());
			userGroups.put(traderNames[i], traderGroups[i]);
		}
		
		try{
			createGroup(new GroupManagement(rmGroups[0].getUser(),traderGroups[0].getUser())
			, rms[0], traders[0]);
			createGroup(new GroupManagement(rmGroups[0].getUser(),traderGroups[1].getUser())
			, rms[0], traders[1]);
			createGroup(new GroupManagement(rmGroups[0].getUser(),traderGroups[2].getUser())
			, rms[0], traders[2]);
			//     rm1
			//		|
			// |----|-----|
			//tr1	tr2   tr3
			
			createGroup(new GroupManagement(rmGroups[1].getUser(),traderGroups[3].getUser())
			, rms[1], traders[3]);
			createGroup(new GroupManagement(rmGroups[1].getUser(),traderGroups[4].getUser())
			, rms[1], traders[4]);
			createGroup(new GroupManagement(rmGroups[1].getUser(),traderGroups[5].getUser())
			, rms[1], traders[5]);
			//     rm2
			//		|
			// |----|-----|
			//tr4	tr5   tr6
			
			
			createGroup(new GroupManagement(rmGroups[7].getUser(),rmGroups[0].getUser())
			, rms[7], rms[0]);
			
			//	   rm8
			//		|
			//     rm1
			//		|
			// |----|-----|
			//tr1	tr2   tr3
			
			createGroup(new GroupManagement(rmGroups[8].getUser(),rmGroups[1].getUser())
			, rms[8], rms[1]);
			
			//	   rm9
			//		|
			//     rm2
			//		|
			// |----|-----|
			//tr4	tr5   tr6
			
			
			createGroup(new GroupManagement(rmGroups[9].getUser(),rmGroups[7].getUser())
			, rms[9], rms[7]);
			createGroup(new GroupManagement(rmGroups[9].getUser(),rmGroups[8].getUser())
			, rms[9], rms[8]);

			//				   rm10
			//	   |------------|---------------|
			//	   rm9						   rm8
			//	   |							|
			//     rm2						   rm1
			//	   |							|
			//|----|-----| 					|---|-----|
			//tr1	tr2   tr3			   tr4	tr5   tr6
					
		}catch(Exception e){
			Assert.fail(e.getMessage());
		}	
	}

	@Test
	public void test() {
		// test recursive isManageRecursive
		// test max level countLevel 
		// isManageeExist
		//getManageeSet
		//putManagee
		//deleteManagee
		//getNoneRecursiveManageeList
	}
	
	@Test
	public void testManageeRecursive(){
		//if rm2 want to manage rm10 
		//use rm10 to check rm2 has been managed first
		boolean isRecusive = rmGroups[9].isManageRecursive(rmGroups[1].getUser());
		Assert.assertTrue(isRecusive);
	}
	
	@Test
	public void testCountLevel(){
		int level = 0;
		
		rmGroups[10].putManagee(rmGroups[9]);// level 5
		rmGroups[11].putManagee(rmGroups[10]);// level 6
		rmGroups[12].putManagee(rmGroups[11]);// level 7
		rmGroups[13].putManagee(rmGroups[12]);// level 8
		rmGroups[14].putManagee(rmGroups[13]);// level 9
		rmGroups[15].putManagee(rmGroups[14]);// level 10
		
		level = rmGroups[15].countLevel(rmGroups[15]);
		
		Assert.assertEquals(UserGroup.MAX_LEVEL, level);
		Assert.assertTrue(UserGroup.MAX_LEVEL < (level+1));
	}
	
	@Test
	public void testGroupPairExist(){
		//equals first level
		Assert.assertTrue(rmGroups[9].isGroupPairExist("rm9"));
	}
	
	@Test
	public void testManageeExist(){
		//search every level
		Assert.assertTrue(rmGroups[9].isManageeExist("trader5"));
	}
	
	@Test
	public void testGetManageeSet(){
		Set <UserGroup> groupSet = rmGroups[9].getManageeSet();
		Assert.assertEquals(10,groupSet.size());
	}
	
	@Test
	public void testPutManagee(){
		rmGroups[9].putManagee(rmGroups[19]);
		Assert.assertEquals(3,rmGroups[9].getNoneRecursiveManageeList().size());
		rmGroups[9].putManagee(rmGroups[19]);
		Assert.assertEquals(3,rmGroups[9].getNoneRecursiveManageeList().size());
	}
	
	@Test
	public void testDeleteManagee(){
		try {
			String deleteUser = "rm8";
			if(rmGroups[9].isGroupPairExist(deleteUser)){
				rmGroups[9].deleteManagee(deleteUser);
				List<UserGroup> userList = rmGroups[9].getNoneRecursiveManageeList();
				Assert.assertEquals(1,userList.size());
			}else{
				Assert.fail("group pair can't found!");
			}
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetNoneRecursiveManageeList(){		
		//first level
		List<UserGroup> userList = rmGroups[9].getNoneRecursiveManageeList();
		Assert.assertEquals(2,userList.size());
	}
	
	
	public void createGroup(GroupManagement gm,User userManager,User userManagee) throws UserException{
		String manager = gm.getManager();
		String managee = gm.getManaged();
		User managerInfo = userManager;
		User manageeInfo = userManagee;
		UserGroup managerGroup = null;
		UserGroup manageeGroup = null;
		
		if(manager.equals(managee)){
			throw new UserException("Manager and Managee are same person!",ErrorMessage.CREATE_GROUP_MANAGEMENT_FAILED);
		}
		
		if( null == managerInfo ){
			throw new UserException("Manager:"+manager+" doen't exist in User",ErrorMessage.CREATE_GROUP_MANAGEMENT_FAILED);
		}
		
		if( null == manageeInfo ){
			throw new UserException("Managee:"+managee+" doen't exist in User",ErrorMessage.CREATE_GROUP_MANAGEMENT_FAILED);
		}
		
		
		if(UserRole.Trader.equals(managerInfo.getRole())){
			throw new UserException("Manager:"+manager+" is a Trader who can't manage someone else",ErrorMessage.CREATE_GROUP_MANAGEMENT_FAILED);
		}
		
		if(!UserRole.RiskManager.equals(managerInfo.getRole())){
			throw new UserException("Manager:"+manager+" role:"+managerInfo.getRole()+" who can't manage someone else",ErrorMessage.CREATE_GROUP_MANAGEMENT_FAILED);
		}
		
		if(!userGroups.containsKey(manager)){
			managerGroup = new UserGroup(manager,managerInfo.getRole());
			userGroups.put(manager, managerGroup);
		}else{
			managerGroup = userGroups.get(manager);
		}
		
		if(!userGroups.containsKey(managee)){
			manageeGroup = new UserGroup(managee,manageeInfo.getRole());
			userGroups.put(managee, manageeGroup);
		}else{
			manageeGroup = userGroups.get(managee);
		}
		
		if(managerGroup.isGroupPairExist(managee)){
			throw new UserException("Managee:"+managee+" already exist!",ErrorMessage.CREATE_GROUP_MANAGEMENT_FAILED);
		}
		
		int level = manageeGroup.countLevel(manageeGroup);
		if(UserGroup.MAX_LEVEL < (level+1) ){
			throw new UserException("Manager:"+manager+", Managee:"+managee+" excceds Max level : "+UserGroup.MAX_LEVEL,ErrorMessage.CREATE_GROUP_MANAGEMENT_FAILED);
		}
		
		/**
		 * if A manage B,check B managees not manage to A 
		 */
		if(!manageeGroup.isManageRecursive(manager)){
			managerGroup.putManagee(manageeGroup);
		}else{
			throw new UserException("Manager:"+manager+", Managee:"+managee+" cause recursive management",ErrorMessage.CREATE_GROUP_MANAGEMENT_FAILED);
		}
	}
	
	
}
