package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service(interfaceClass = GoodsService.class)
@Transactional
public class GoodsServiceImpl extends BaseServiceImpl<TbGoods> implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsDescMapper goodsDescMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ItemCatMapper itemCatMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SellerMapper sellerMapper;

    @Override
    public PageResult search(Integer page, Integer rows, TbGoods goods) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();

        //不查询删除商品
        criteria.andNotEqualTo("isDelete","1");

        if (!StringUtils.isEmpty(goods.getSellerId())) {
            criteria.andLike("sellerId", "%" + goods.getSellerId() + "%");
        }
        if (!StringUtils.isEmpty(goods.getAuditStatus())) {
            criteria.andLike("auditStatus", "%" + goods.getAuditStatus() + "%");
        }
        if (!StringUtils.isEmpty(goods.getGoodsName())) {
            criteria.andLike("goodsName", "%" + goods.getGoodsName() + "%");
        }

        List<TbGoods> list = goodsMapper.selectByExample(example);
        PageInfo<TbGoods> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 更新商品删除状态为已删除
     * @param ids
     */
    @Override
    public void deleteGoodByIds(Long[] ids) {
        TbGoods goods = new TbGoods();
        //将删除状态设置为1，也就是已删除状态
        //update tb_goods set id_delete ='1' where id in (?,?,?...)
        goods.setIsDelete("1");
        Example example = new Example(TbGoods.class);
        example.createCriteria().andIn("id",Arrays.asList(ids));
        //批量更新商品状态为已删除
        goodsMapper.updateByExampleSelective(goods,example);

    }

    /**
     * 商品的基本、描述、sku列表信息之后要保存到数据库中。
     * @param id 商品信息  //这里其实回显功能的实现
     * @return 操作结果
     */
    @Override
    public Goods findGoodsById(Long id) {
        Goods goods = new Goods();
        //基本信息
        goods.setGoods(findOne(id));
        //描述信息
        goods.setGoodsDesc(goodsDescMapper.selectByPrimaryKey(id));
        //根据spu id 查询sku列表
        TbItem item = new TbItem();
        item.setGoodsId(id);
        List<TbItem> itemList = itemMapper.select(item);
        goods.setItemList(itemList);
        return goods;
    }

    /**
     * 更新商品功能
     * @param goods 商品
     */
    @Override
    public void updateGoods(Goods goods) {
        goods.getGoods().setAuditStatus("0");//修改过的则重新设置未审核
        //更新商品基本信息
        goodsMapper.updateByPrimaryKeySelective(goods.getGoods());
        //更新商品描述信息
        goodsDescMapper.updateByPrimaryKeySelective(goods.getGoodsDesc());
        //删除原有的SKU表
        TbItem param = new TbItem();
        param.setGoodsId(goods.getGoods().getId());
        itemMapper.delete(param);
        //保存商品SKU列表
        saveItemList(goods);
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        //update tb_goods set audit_status-1 where id in(?,?,?)
        TbGoods goods = new TbGoods();
        goods.setAuditStatus(status);

        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();
        //设置查询条件:id
        criteria.andIn("id", Arrays.asList(ids));
        //参数1：更新对象，参数2：更新条件
        goodsMapper.updateByExampleSelective(goods,example);
        //如果是审核通过则将SKU设置为启用状态
        if ("2".equals(status)){
            //更新内容
            TbItem item = new TbItem();
            item.setStatus("1");

            //更新条件
            Example itemExample = new Example(TbItem.class);
            itemExample.createCriteria().andIn("goodsId",Arrays.asList(ids));
            itemMapper.updateByExampleSelective(item,itemExample);
        }
    }


    /**
     * 商品的基本、描述、sku列表信息之后要保存到数据库中。
     *
     * @param goods 商品信息
     * @return 操作结果
     */
    @Override
    public void addGoods(Goods goods) {
        //1. 保存商品基本
        goodsMapper.insertSelective(goods.getGoods());
        //2. 保存商品描述信息
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        //3. 保存商品sku列表（每个sku都要保存到tb_item
        goodsDescMapper.insertSelective(goods.getGoodsDesc());

        //新增商品SKU

        saveItemList(goods);
    }

    /**
     * 保存动态sku列表
     *
     * @param goods 商品信息（基本，描述，sku列表）
     */
    private void saveItemList(Goods goods) {
        if (goods.getItemList() != null && goods.getItemList().size() > 0) {
            for (TbItem item : goods.getItemList()) {
                //标题=sku名称+所有的规格的选项值
                String title = goods.getGoods().getGoodsName();
                //获取规格
                Map<String, String> map = JSON.parseObject(item.getSpec(), Map.class);
                Set<Map.Entry<String, String>> entries = map.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    title += " " + entry.getValue();
                }
                item.setTitle(title);

                //设置商品
                setItemValue(item, goods);

                //保存sku
                itemMapper.insertSelective(item);
            }
        } else {
            //未启用规格
            //1.创建item对象：大多数数据来自spu设置到对象中
            TbItem tbItem = new TbItem();
            //2.如果spu中没有的数据，如spec({}),num(9999),status(0未启用),isDefault(1默认)
            tbItem.setTitle(goods.getGoods().getGoodsName());
            tbItem.setPrice(goods.getGoods().getPrice());
            tbItem.setNum(9999);
            tbItem.setStatus("0");
            tbItem.setIsDefault("1");
            tbItem.setSpec("{}");

            setItemValue(tbItem, goods);
            //3.保存到数据库中
            itemMapper.insertSelective(tbItem);
        }
    }

    private void setItemValue(TbItem item, Goods goods) {
        //商品分类 来自商品spu的第三级商品分类id
        item.setCategoryid(goods.getGoods().getCategory3Id());
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(item.getCategoryid());
        item.setCategory(itemCat.getName());

        //图片：可以从spu中的图片地址列表中获得第一张图片
        if (!StringUtils.isEmpty(goods.getGoodsDesc().getItemImages())) {
            List<Map> imageList = JSONArray.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
            if (imageList.get(0).get("url")!=null){
                item.setImage(imageList.get(0).get("url").toString());
            }
        }
        item.setGoodsId(goods.getGoods().getId());
        //品牌
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(brand.getName());
        //设置上传时间
        item.setCreateTime(new Date());
        item.setUpdateTime(item.getCreateTime());
        //卖家
        item.setSellerId(goods.getGoods().getSellerId());
        TbSeller seller = sellerMapper.selectByPrimaryKey(item.getSellerId());
        item.setSeller(seller.getName());
    }
}
