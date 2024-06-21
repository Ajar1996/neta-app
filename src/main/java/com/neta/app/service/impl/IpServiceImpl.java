package com.neta.app.service.impl;

import com.neta.app.entity.Ip;
import com.neta.app.mapper.IpMapper;
import com.neta.app.service.IIpService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author springBoot-Learning
 * @since 2024-06-21
 */
@Service
public class IpServiceImpl extends ServiceImpl<IpMapper, Ip> implements IIpService {

}
