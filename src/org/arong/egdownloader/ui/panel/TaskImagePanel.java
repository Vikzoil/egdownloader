package org.arong.egdownloader.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.arong.egdownloader.model.Task;
import org.arong.egdownloader.model.TaskStatus;
import org.arong.egdownloader.ui.FontConst;
import org.arong.egdownloader.ui.swing.AJButton;
import org.arong.egdownloader.ui.swing.AJLabel;
import org.arong.egdownloader.ui.swing.AJPager;
import org.arong.egdownloader.ui.swing.AJPanel;
import org.arong.egdownloader.ui.table.TaskingTable;
import org.arong.egdownloader.ui.window.EgDownloaderWindow;
import org.arong.egdownloader.ui.work.CommonSwingWorker;
import org.arong.egdownloader.ui.work.ReCreateWorker;
import org.arong.util.Tracker;
/**
 * 任务的图片模式展示面板
 * @author Administrator
 *
 */
public class TaskImagePanel extends AJPanel {
	private EgDownloaderWindow mainWindow;
	public int selectIndex = 0;
	public static final int PAGESIZE = 200;
	public int page = 1;
	private FlowLayout layout = new FlowLayout(FlowLayout.CENTER);
	public AJPager imageTaskPager;
	//public JPanel container;
	private static Color progressBarBorder = new Color(47, 110, 178);
	private static Color progressBarBorder2 = new Color(65, 145, 65);//已完成颜色
	public TaskImagePanel(final EgDownloaderWindow mainWindow){
		this.mainWindow = mainWindow;
		this.setLayout(layout);
		this.setLocation(10,  5);
		this.setPreferredSize(new Dimension(mainWindow.getWidth() - 20, 10000));
		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				//init(mainWindow.tasks);
			}
		});
		
		//初始化组件
		init(mainWindow.tasks);
	}
	public void changeViewSize(){
		final TaskImagePanel this_ = this;
		if(this.getComponents() != null){
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					new CommonSwingWorker(new Runnable() {
						public void run() {
							this_.scrollRectToVisible(new Rectangle(0, 0));
							for(int i = 0; i < this_.getComponents().length; i ++){
								if(this_.getComponents()[i] instanceof AJPanel){
									AJPanel p = (AJPanel) this_.getComponents()[i];
									for(int j = 0; j < p.getComponents().length; j ++){
										if(p.getComponents()[j].getName() != null && p.getComponents()[j].getName().startsWith("cover")){
											AJLabel l = (AJLabel) p.getComponents()[j];
											ImageIcon icon = (ImageIcon) l.getIcon();
											if(icon != null){
												double w = icon.getIconWidth() > mainWindow.setting.getCoverWidth() ? mainWindow.setting.getCoverWidth() : icon.getIconWidth();
												int height = w > icon.getIconWidth() ? icon.getIconHeight() : (int)(icon.getIconHeight() * (w / icon.getIconWidth()));
												l.setSize((int)w, height > mainWindow.setting.getCoverHeight() ? mainWindow.setting.getCoverHeight() : height);
												l.setPreferredSize(new Dimension((int)w, height > mainWindow.setting.getCoverHeight() ? mainWindow.setting.getCoverHeight() : height));
												//icon.getImage().flush();//解决加载图片不完全问题
												l.setImage(icon);
												l.setIcon(icon);
											}
											break;
										}
									}
									//p.updateUI();
								}
							}
							this_.updateUI();
							try {
								Thread.sleep(100);
								int maxheight = 0;
								for(int i = 0; i < this_.getComponents().length; i ++){
									if(maxheight < ((int)this_.getComponents()[i].getLocation().getY() + this_.getComponents()[i].getHeight())){
										maxheight = ((int)this_.getComponents()[i].getLocation().getY() + this_.getComponents()[i].getHeight());
									}
								}
								this_.setPreferredSize(new Dimension(this_.getWidth(), maxheight + 10));	
								this_.updateUI();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}}).execute();;
				}
			});
		}
	}
	public void init(){
		init(mainWindow.tasks);
	}
	public void flush(final Task task){
		flush(task, false);
	}
	public void flush(final Task task, final boolean iconload){
		final TaskImagePanel this_= this;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new CommonSwingWorker(new Runnable() {
					public void run() {
						Component[] oldcomps = this_.getComponents();
						AJPanel p = null;
						for(Component comp : oldcomps){
							if(comp.getName() != null && comp.getName().startsWith(task.getId() + "|")){
								p = (AJPanel)comp;
								break;
							}
						}
						if(p != null){
							AJLabel l = (AJLabel)p.getComponent(0);
							if(iconload || l.getIcon() == null){
								String path = task.getSaveDir() + "/cover.jpg";
								File cover = new File(path);
								if(cover != null && cover.exists()){
									try{
										ImageIcon icon = new ImageIcon(ImageIO.read(cover));
										double w = icon.getIconWidth() > mainWindow.setting.getCoverWidth() ? mainWindow.setting.getCoverWidth() : icon.getIconWidth();
										int height = w > icon.getIconWidth() ? icon.getIconHeight() : (int)(icon.getIconHeight() * (w / icon.getIconWidth()));
										l.setSize((int)w, height > mainWindow.setting.getCoverHeight() ? mainWindow.setting.getCoverHeight() : height);
										l.setPreferredSize(new Dimension((int)w, height > mainWindow.setting.getCoverHeight() ? mainWindow.setting.getCoverHeight() : height));
										//icon.getImage().flush();//解决加载图片不完全问题
										l.setImage(icon);
										l.setIcon(icon);
									}catch(Exception e){
										e.printStackTrace();
									}
								}
							}
							JProgressBar bar = (JProgressBar)p.getComponent(1);
							bar.setValue(task.getCurrent());
							bar.setString(getTaskInfo(task));
							if(task.getStatus() == TaskStatus.COMPLETED){
								bar.setForeground(progressBarBorder2);
							}
						}
					}
				}).execute();
			}
		});
	}
	public void init(final List<Task> tasks){
		final TaskImagePanel this_ = this;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new CommonSwingWorker(new Runnable() {
					public void run() {
						Component[] oldcomps = this_.getComponents();
						this_.removeAll();
						if(tasks != null && tasks.size() > 0){
							this_.scrollRectToVisible(new Rectangle(0, 0));
							List<Task> ptasks = new ArrayList<Task>();
							for(int i = (page - 1) * PAGESIZE; i < page * PAGESIZE && i < tasks.size(); i ++){
								ptasks.add(tasks.get(i));
							}
							//this_.setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width - 20, ((ptasks.size() / (Toolkit.getDefaultToolkit().getScreenSize().width / mainWindow.setting.getCoverWidth())) + 8) * (mainWindow.setting.getCoverHeight() + 20)));
							if(imageTaskPager != null){
								imageTaskPager.setVisible(false);
							}
							selectIndex = mainWindow.runningTable.getSelectedRow();
							List<AJPanel> panels = new ArrayList<AJPanel>();
							for(int i = 0; i < ptasks.size(); i ++){
								//判断AJPanel是否存在
								//AJPanel p = null;
								AJPanel p = null;
								for(Component comp : oldcomps){
									if(comp.getName() != null && comp.getName().startsWith(ptasks.get(i).getId() + "|")){
										p = (AJPanel)comp;
										break;
									}
								}
								//获取选中行
								boolean selected = ((page - 1) * PAGESIZE + i) == selectIndex;
								if(p != null){
									//name规则：taskID|list索引
									p.setName(ptasks.get(i).getId() + "|" + ((page - 1) * PAGESIZE + i));
									p.setBorder(BorderFactory.createLineBorder(selected ? Color.RED : Color.BLACK, 2));
									JProgressBar bar = (JProgressBar)p.getComponent(1);
									bar.setString(getTaskInfo(ptasks.get(i)));
								}else{
									p = new AJPanel();
									//name规则：taskID|list索引
									p.setName(ptasks.get(i).getId() + "|" + ((page - 1) * PAGESIZE + i));
									p.setToolTipText(ptasks.get(i).getDisplayName(mainWindow.setting));
									p.setLayout(new BorderLayout());
									p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
									p.setBackground(Color.WHITE);
									p.setForeground(Color.WHITE);
									p.setBorder(BorderFactory.createLineBorder(selected ? Color.RED : Color.BLACK, 2));
									p.addMouseListener(new MouseAdapter() {
										public void mouseClicked(MouseEvent e) {
											for(int i = 0; i < this_.getComponents().length; i ++){
												//((AJPanel)this_.getComponents()[i]).setBackground(Color.WHITE);
												if(this_.getComponents()[i] instanceof AJPanel){
													((AJPanel)this_.getComponents()[i]).setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
												}
											}
											AJPanel p = (AJPanel)e.getSource();
											p.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
											selectIndex = Integer.parseInt(p.getName().split("\\|")[1]);
											//同步任务表格的选中状态
											mainWindow.runningTable.setRowSelectionInterval(selectIndex, selectIndex);
											if(e.getButton() == MouseEvent.BUTTON3){
												Task task = mainWindow.runningTable.getTasks().get(selectIndex);
												mainWindow.tablePopupMenu.show(task, p, e.getPoint().x - 1, e.getPoint().y - 1);
											}else{
												//双击切换
												if(e.getClickCount() == 2){
													//任务开始或暂停
													if(mainWindow.runningTable.rebuild){
														Tracker.println(TaskingTable.class, "正在重建任务");
														return;
													}
													Task task = mainWindow.runningTable.getTasks().get(selectIndex);
													//如果状态为未开始或者已暂停，则将状态改为下载中，随后开启下载线程
													if(task.getStatus() == TaskStatus.UNSTARTED || task.getStatus() == TaskStatus.STOPED){
														mainWindow.runningTable.startTask(task);
													}
													//如果状态为下载中，则将状态改为已暂停，随后将下载线程取消掉
													else if(task.getStatus() == TaskStatus.STARTED || task.getStatus() == TaskStatus.WAITING){
														mainWindow.runningTable.stopTask(task);
													}
													//如果状态为未创建，则开启创建线程
													else if(task.getStatus() == TaskStatus.UNCREATED){
														Tracker.println(getClass(), task.getName() + ":重新采集");
														task.setReCreateWorker(new ReCreateWorker(task, mainWindow));
														task.getReCreateWorker().execute();
													}
													
													//mainWindow.infoTabbedPane.setSelectedIndex(1);
												}else{
													//切换信息面板tab
													if(mainWindow.infoTabbedPane.getSelectedIndex() == 1){
														mainWindow.taskInfoPanel.parseTask(mainWindow.tasks.get(selectIndex), selectIndex);
													}else if(mainWindow.infoTabbedPane.getSelectedIndex() == 2){
														TaskTagsPanel panel = (TaskTagsPanel) mainWindow.infoTabbedPane.getComponent(2);
														panel.showTagGroup(mainWindow.tasks.get(selectIndex));
													}else if(mainWindow.infoTabbedPane.getSelectedIndex() == 3){
														PicturesInfoPanel infoPanel = (PicturesInfoPanel) mainWindow.infoTabbedPane.getComponent(3);
														infoPanel.showPictures(mainWindow.tasks.get(selectIndex));
													}
												}
											}
										}
										public void mouseEntered(MouseEvent e) {
											AJPanel p = (AJPanel)e.getSource();
											//p.setBackground(Color.ORANGE);
											p.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
										}

										public void mouseExited(MouseEvent e) {
											AJPanel p = (AJPanel)e.getSource();
											if(selectIndex != Integer.parseInt(p.getName().split("\\|")[1])){
												p.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
											}else{
												p.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
											}
										}
									});
									AJLabel l = new AJLabel();
									l.setName("cover" + ptasks.get(i).getId());
									l.setOpaque(true);
									l.setBackground(Color.BLACK);
									l.setForeground(Color.WHITE);
									l.setFont(FontConst.Microsoft_BOLD_12);
									l.setVerticalTextPosition(JLabel.TOP);
									//l.setHorizontalTextPosition(JLabel.LEADING);
									//Border border = BorderFactory.createLineBorder(Color.BLACK);
									//l.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
									
									
									/*try{
										ImageIcon icon = IconManager.getIcon("init");
										double w = icon.getIconWidth() > mainWindow.setting.getCoverWidth() ? mainWindow.setting.getCoverWidth() : icon.getIconWidth();
										int height = w > icon.getIconWidth() ? icon.getIconHeight() : (int)(icon.getIconHeight() * (w / icon.getIconWidth()));
										l.setSize((int)w, height > mainWindow.setting.getCoverHeight() ? mainWindow.setting.getCoverHeight() : height);
										l.setPreferredSize(new Dimension((int)w, height > mainWindow.setting.getCoverHeight() ? mainWindow.setting.getCoverHeight() : height));
										//icon.getImage().flush();//解决加载图片不完全问题
										l.setImage(icon);
										l.setIcon(icon);
									}catch(Exception e){
										e.printStackTrace();
									}*/
									final String path = ptasks.get(i).getSaveDir() + "/cover.jpg";
									File cover = new File(path);
									if(cover != null && cover.exists()){
										try{
											ImageIcon icon = new ImageIcon(ImageIO.read(cover));
											double w = icon.getIconWidth() > mainWindow.setting.getCoverWidth() ? mainWindow.setting.getCoverWidth() : icon.getIconWidth();
											int height = w > icon.getIconWidth() ? icon.getIconHeight() : (int)(icon.getIconHeight() * (w / icon.getIconWidth()));
											l.setSize((int)w, height > mainWindow.setting.getCoverHeight() ? mainWindow.setting.getCoverHeight() : height);
											l.setPreferredSize(new Dimension((int)w, height > mainWindow.setting.getCoverHeight() ? mainWindow.setting.getCoverHeight() : height));
											//icon.getImage().flush();//解决加载图片不完全问题
											l.setImage(icon);
											l.setIcon(icon);
										}catch(Exception e){
											e.printStackTrace();
										}
									}
									
									//AJLabel l2 = new AJLabel(getTaskInfo(ptasks.get(i)), Color.BLACK);
									//l2.setBorder(new EmptyBorder(-4, 5, 1, 0));
									JProgressBar bar = new JProgressBar(0, ptasks.get(i).getTotal());
									bar.setBackground(Color.WHITE);
									bar.setString(getTaskInfo(ptasks.get(i)));
									bar.setStringPainted(true);
									bar.setFont(FontConst.Microsoft_BOLD_11);bar.setPreferredSize(new Dimension(110, 13));
									if(ptasks.get(i).getStatus() == TaskStatus.COMPLETED){
										bar.setForeground(progressBarBorder2);
									}else{
										bar.setForeground(progressBarBorder);
									}
									//bar.setBorder(BorderFactory.createLineBorder(progressBarBorder));
									bar.setValue(ptasks.get(i).getCurrent());
									p.add(l, BorderLayout.SOUTH);
									p.add(bar, BorderLayout.NORTH);
								}
								panels.add(p);
								this_.add(p, i);
								this_.updateUI();
							}
							try {
								Thread.sleep(100);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
							int maxheight = 0;
							for(JPanel p : panels){
								if(maxheight < ((int)p.getLocation().getY() + p.getHeight())){
									maxheight = ((int)p.getLocation().getY() + p.getHeight());
								}
							}
							this_.setPreferredSize(new Dimension(this_.getWidth(), maxheight + 10));
							this_.updateUI();
							
							int totalPage = tasks.size() % PAGESIZE == 0 ? tasks.size() / PAGESIZE : tasks.size() / PAGESIZE + 1;
							if(totalPage > 1){
								if(imageTaskPager == null){
									imageTaskPager = new AJPager(40 , mainWindow.infoTabbedPane.getY() + 25, (int)(mainWindow.taskInfoPanel.getWidth() - 80), 40, new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											JButton btn = (JButton) e.getSource();
											this_.page = Integer.parseInt(btn.getName());
											this_.init();
										}
									});
									((FlowLayout)imageTaskPager.getLayout()).setAlignment(FlowLayout.RIGHT);
									JButton btn = new AJButton("顶部⇧");
									btn.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											mainWindow.tablePane.getVerticalScrollBar().setValue(0);
										}
									});
									imageTaskPager.setMaxPage(6);
									imageTaskPager.setExt(new JComponent[]{btn});
									imageTaskPager.setOpaque(false);
									mainWindow.getLayeredPane().add(imageTaskPager, 10, 0);
								}
								imageTaskPager.setTotal(tasks.size());
								imageTaskPager.change(totalPage, page);
								imageTaskPager.updateUI();
								imageTaskPager.setVisible(true);
							}
						}
						//System.gc();
					}
				}).execute();
			}
		});
	}
	
	private String getTaskInfo(Task task){
		StringBuffer txtsb = new StringBuffer("");
		if(task.isSearched()){
			txtsb.append("[搜] ");
		}
		if(task.getStatus() != TaskStatus.COMPLETED && task.getCurrent() > 0){
			txtsb.append(task.getCurrent()).append("/");
		} 
		txtsb.append(task.getTotal()).append(" ");
		if(task.getStatus() == TaskStatus.WAITING){
			txtsb.append(TaskStatus.WAITING.getStatus()).append(" ");
		}
		/*String statusColor = "";
		if(task.getStatus() == TaskStatus.UNSTARTED){
			statusColor = "#5f392d";
		}else if(task.getStatus() == TaskStatus.STARTED){
			statusColor = "#4192ff";
		}else if(task.getStatus() == TaskStatus.STOPED){
			statusColor = "#000159";
		}else if(task.getStatus() == TaskStatus.COMPLETED){
			statusColor = "#419141";
		}else if(task.getStatus() == TaskStatus.WAITING){
			statusColor = "#d2691e";
		}*/
		if(task.getStatus() == TaskStatus.STARTED){
			txtsb.append(task.getDownSpeed().toLowerCase());
		}/*else{
			txtsb.append(task.getStatus().getStatus());
		}*/
		if(task.isReaded()){
			txtsb.append(" √");
		}
		//txtsb.append("</b></html>");
		return txtsb.toString();
	}
}
