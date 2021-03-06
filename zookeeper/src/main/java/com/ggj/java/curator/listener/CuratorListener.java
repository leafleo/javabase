package com.ggj.java.curator.listener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;

import com.ggj.java.curator.CuratorUtil;

/**
 * Curator提供了三种Watcher(Cache)来监听结点的变化.
 * Path Cache：监视一个路径下1）孩子结点的创建、2）删除，3）以及结点数据的更新。产生的事件会传递给注册的PathChildrenCacheListener。
 Node Cache：监视一个结点的创建、更新、删除，并将结点的数据缓存在本地。
 Tree Cache：Path Cache和Node Cache的“合体”，监视路径下的创建、更新、删除事件，并缓存路径下所有孩子结点的数据
 * @author:gaoguangjin
 * @date 2016/8/2 10:54
 */
@Slf4j
public class CuratorListener {
	
	private static final String LISTERNER_PATH = "/root/listener1";
	
	private static final String LISTERNER_CHILD_PATH = "/child5";
	
	/**
	 * 在注册监听器的时候，如果传入此参数，当事件触发时，逻辑由线程池处理
	 */
	static ExecutorService pool = Executors.newFixedThreadPool(2);
	
	public static void main(String[] args) throws Exception {
		CuratorFramework client = CuratorUtil.getClient();
        if(!CuratorUtil.checkExists(client,LISTERNER_PATH))
		client.create().creatingParentsIfNeeded().forPath(LISTERNER_PATH, "hello".getBytes());
		/**
		 * 监听数据节点的变化情况
		 */
		final NodeCache nodeCache = new NodeCache(client, LISTERNER_PATH, false);
		nodeCache.start(true);
		nodeCache.getListenable().addListener(new NodeCacheListener() {
			@Override
			public void nodeChanged() throws Exception {
				log.info("Node data is changed, new data: " + new String(nodeCache.getCurrentData().getData()));
			}
		}, pool);


		/**
		 * 监听子节点的变化情况
		 */
		final PathChildrenCache childrenCache = new PathChildrenCache(client, LISTERNER_PATH, true);
		childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
		childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				switch(event.getType()) {
					case CHILD_ADDED:
                        log.info("CHILD_ADDED: " + event.getData().getPath());
						break;
					case CHILD_REMOVED:
                        log.info("CHILD_REMOVED: " + event.getData().getPath());
						break;
					case CHILD_UPDATED:
                        log.info("CHILD_UPDATED: " + event.getData().getPath());
						break;
					default:
						break;
				}
			}
		}, pool);
		client.setData().forPath(LISTERNER_PATH, "world".getBytes());
        if(!CuratorUtil.checkExists(client,LISTERNER_CHILD_PATH))
        client.create().creatingParentsIfNeeded().forPath(LISTERNER_PATH+LISTERNER_CHILD_PATH, "child".getBytes());
		Thread.sleep(10 * 1000);
		pool.shutdown();
		client.close();
	}
}
