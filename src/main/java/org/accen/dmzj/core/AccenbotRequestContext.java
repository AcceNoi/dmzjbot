package org.accen.dmzj.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.accen.dmzj.core.annotation.CmdRequest;
import org.accen.dmzj.core.autoconfigure.EventCmdPostProcessor;
import org.accen.dmzj.core.exception.CmdRegisterDuplicateException;
import org.accen.dmzj.core.exception.CmdRegisterException;
import org.accen.dmzj.core.meta.PostType;
import org.accen.dmzj.core.meta.RequestSubType;
import org.accen.dmzj.core.meta.RequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AccenbotRequestContext extends AccenbotContext {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	Map<String,AccenbotCmdProxy> requestCmdProxyIndex = new HashMap<>();
	private AccenbotContext parentContext;
	public AccenbotRequestContext(@Autowired @Qualifier("accenbotContext")AccenbotContext parentContext) {
		this.parentContext = parentContext;
		parentContext.registerContext(PostType.REQUEST, this);
	}
	
	@Override
	public void acceptEvent(Map<String, Object> event) {
		RequestType requestType = RequestType.valueOf(((String)event.get("request_type")).toUpperCase());
		RequestSubType subType = event.containsKey("sub_type")?RequestSubType.valueOf(((String)event.get("sub_type")).toUpperCase()):RequestSubType._ALL;
		super.myProxies().stream().forEach(proxy->{
			if(Arrays.stream(((CmdRequest)proxy.anno()).requestType())
						.anyMatch(avaliableRequestType -> avaliableRequestType == RequestType._ALL || avaliableRequestType == requestType)
				&&
				Arrays.stream(((CmdRequest)proxy.anno()).subType())
						.anyMatch(avaliableSubType -> avaliableSubType == RequestSubType._ALL || avaliableSubType == subType)
			) {
				//cmd执行前的预处理，可以控制是否执行
				if(!parentContext.eventCmdPostProcessors.parallelStream().allMatch(p->p.beforeEventCmdPost(proxy, event))) {
					return;
				}
				try {
					Object[] params = Arrays.stream(proxy.cmdMethod().getParameters())
							.map(p->{
								Object lastParameterValue = null;
								for(EventCmdPostProcessor processor:parentContext.eventCmdPostProcessors) {
									lastParameterValue = processor.eventCmdParamPost(proxy, event, p, lastParameterValue);
								}
								return lastParameterValue;
							})
							.toArray(Object[]::new);
					Object rs = proxy.cmdMethod().invoke(proxy.cmd(),params);
					//cmd执行后的后处理，可以对cmd结果进行格式化，但是此方法比较危险，EventCmdPostProcessor互相可以影响
					for(EventCmdPostProcessor p:parentContext.eventCmdPostProcessors) {
						rs = p.afterEventCmdPost(proxy, event, rs);
					}
					/*boolean isGroup = Set.of("group").contains(event.get("request_type"));
					GeneralTask[] tasks = super.generalMessage(rs, proxy.cmdMethod()
							, isGroup?"group":"private"
							, isGroup?""+event.get("group_id"):""+event.get("user_id")
							, ""+event.get("self_id"));
					taskManager.addGeneralTasks(tasks);*/
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void parseAndRegisterClass(Object bean) {
		if(bean.getClass().isAnnotationPresent(CmdRequest.class)) {
			CmdRequest cmdRequest= bean.getClass().getDeclaredAnnotation(CmdRequest.class);
			String name = cmdRequest.value();
			String[] executeMethods = cmdRequest.executeMethod();
			if(name!=null&&name.trim().equals("")&&executeMethods.length>1) {
				throw new CmdRegisterException("当定义了name时，仅允许定义一个executeMethod，");
			}
			Map<String,List<Method>> methodMap = Arrays.stream(bean.getClass().getDeclaredMethods())
											.filter(method->!method.isAnnotationPresent(CmdRequest.class))//如果方法已经被@CmdMessage注解了则由registerMethod处理
											.collect(Collectors.groupingBy(Method::getName));
			
			if(executeMethods.length>0) {
				Arrays.stream(executeMethods)
						.forEach(executeMethod->{
							if(methodMap.keySet().contains(executeMethod)) {
								if(name==null||name.trim().equals("")) {
									//name为空，则所有重载了此方法名的方法全部注册进去
									IntStream.range(0, methodMap.get(executeMethod).size())
												.forEach(index->{
													String uniqueName = defineMethodName(methodMap.get(executeMethod).get(index),bean.getClass());
													try {
														String name1 = parseAndRegisterMethod(
																bean, 
																methodMap.get(executeMethod).get(index),index==0?uniqueName:(uniqueName+":"+(index-1))
														);
														logger.info("CmdRequest注册成功!name：{}",name1);
													} catch (CmdRegisterDuplicateException e) {
														logger.warn(e.getMessage());
													}
												});
									
								}else {
									//name不为空
									try {
										String name1 = parseAndRegisterMethod(bean,methodMap.get(executeMethod).get(0),name);
										logger.info("CmdRequest注册成功!name：{}",name1);
									} catch (CmdRegisterDuplicateException e) {
										logger.warn(e.getMessage());
									}
								}
								
							}
						});
			}
		}
	}
	@Override
	public String parseAndRegisterMethod(Object bean,Method method) throws CmdRegisterDuplicateException{
		if(method.isAnnotationPresent(CmdRequest.class)) {
			CmdRequest cr = method.getDeclaredAnnotation(CmdRequest.class);
			String name = cr.value();
			if(name==null||name.isBlank()) {
				name = defineMethodName(method, bean.getClass());
			}
			return parseAndRegisterMethod( bean
					, method
					, name
					,method.getDeclaredAnnotation(CmdRequest.class));
		}else {
			return null;
		}
	}
	/**
	 * 注册一个cmdProxy
	 * @param bean
	 * @param method
	 * @return name
	 */
	public String parseAndRegisterMethod(Object bean,Method method,Object anno) throws CmdRegisterDuplicateException{
		return parseAndRegisterMethod( bean, method,defineMethodName(method, bean.getClass()),anno);
	}
	/**
	 * 注册一个cmdProxy
	 * @param bean
	 * @param method
	 * @param name
	 * @return name
	 */
	public String parseAndRegisterMethod(Object bean,Method method,String name,Object anno) throws CmdRegisterDuplicateException{
		if(requestCmdProxyIndex.containsKey(name)) {
			throw new CmdRegisterDuplicateException(name, bean.getClass(), method);
		}else {
			registerMyCmdProxy(new AccenbotCmdProxy(name,bean, method, anno,CmdRequest.class));
			return name;
		}
	}
	
}
