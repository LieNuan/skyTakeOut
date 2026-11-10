package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class AddressBookServiceImpl implements AddressBookService {
    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 条件查询
     *
     * @param addressBook
     * @return
     */
    @Override
    public List<AddressBook> list(AddressBook addressBook) {
        return addressBookMapper.list(addressBook);
    }

    @Override
    public void add(AddressBook addressBook) {
        //用户id由后端从token中获取，新增的地址默认不是默认地址
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        addressBookMapper.add(addressBook);
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    @Override
    public AddressBook getById(Long id) {
        AddressBook addressBook = addressBookMapper.getById(id);
        return addressBook;
    }

    @Override
    public void updateById(AddressBook addressBook) {
        //带上当前登录用户id，防止修改到其他用户的地址
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.updateById(addressBook);
    }

    /**
     * 设置默认地址
     *
     * @param addressBook
     */
    @Override
    @Transactional
    public void setDefault(AddressBook addressBook) {
        //1、将当前用户的所有地址修改为非默认地址 update address_book set is_default = ? where user_id = ?
        addressBook.setIsDefault(0);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.updateIsDefaultByUserId(addressBook);

        //2、将当前地址改为默认地址 update address_book set is_default = ? where id = ?
        addressBook.setIsDefault(1);
        addressBookMapper.updateById(addressBook);
    }

    /**
     * 根据id删除地址
     *
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }

}
