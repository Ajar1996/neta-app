package com.neta.app.service.impl;

import com.neta.app.entity.Event;
import com.neta.app.mapper.EventMapper;
import com.neta.app.service.IEventService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author springBoot-Learning
 * @since 2024-06-07
 */
@Service
public class EventServiceImpl extends ServiceImpl<EventMapper, Event> implements IEventService {

}
