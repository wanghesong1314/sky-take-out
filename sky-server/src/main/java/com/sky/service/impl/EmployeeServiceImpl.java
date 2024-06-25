package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import sun.util.resources.LocaleData;

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1.创建lambda的条件查询器
        LambdaQueryWrapper<Employee> qwl = new LambdaQueryWrapper();
        qwl.eq(Employee::getUsername,username);
        Employee employee = employeeMapper.selectOne(qwl);

//        //1、根据用户名查询数据库中的数据,如果用传统的方式等价于上面的mybatis-plus方式
//        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对,进行MD5加密
        password=DigestUtils.md5DigestAsHex(password.getBytes());
        // TODO 后期需要进行md5加密，然后再进行比对
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }


    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult employeePageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
//        创建条件语句
        LambdaQueryWrapper<Employee> lwq=new LambdaQueryWrapper();
//        插入条件
        lwq.like(employeePageQueryDTO.getName()!=null,Employee::getName,employeePageQueryDTO.getName());
        lwq.orderByDesc(Employee::getCreateTime);
//        分页
        IPage<Employee> page=new Page(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
//        启动查询，会被拦截器拦截加入到page中
        employeeMapper.selectPage(page, lwq);
//        封装结果
        PageResult pageResult=new PageResult(page.getTotal(),page.getRecords());
        return pageResult;
    }

    /**
     * 新增员工
     * @param employee
     */
    @Override
    public void save(Employee employee) {
        employeeMapper.insert(employee);
    }


    /**
     * 根据id改变状态
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
//        建造者模式的风格
        Employee employee = Employee.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .build();
        employeeMapper.updateById(employee);
    }

    @Override
    public Employee getById(Integer id) {
        Employee employee = employeeMapper.selectById(id);
        return employee;
    }

    @Override
    public void update(Employee employee) {
        employeeMapper.updateById(employee);
    }
}
