package org.hudson.trayapp.gui;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.hudson.trayapp.HudsonTrayApp;
import org.hudson.trayapp.gui.icons.Icons;
import org.hudson.trayapp.gui.tray.AnimatedTrayIcon;
import org.hudson.trayapp.gui.tray.HudsonTrayIconHelper;
import org.hudson.trayapp.model.Job;
import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.DesktopException;
import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

public class Tray {
	private SystemTray tray;
	private TrayIcon trayIcon;
	
	private AnimatedTrayIcon animatedTrayIcon;

	public Tray() {
		try {
			tray = SystemTray.getDefaultSystemTray();
			Image image;
			image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/org/hudson/trayapp/gui/icons/tray/hudson.png"));

		    trayIcon = new TrayIcon(new ImageIcon(image));
		    HudsonTrayIconHelper.prepare(trayIcon);
		    setToolTip("");
		    rebuildPopupMenu(new Vector(0));

		    ActionListener actionListener = new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		        	showMainWindow();
		        }
		    };

		    trayIcon.setIconAutoSize(true);
		    trayIcon.addActionListener(actionListener);
		    tray.addTrayIcon(trayIcon);
		    
		} catch (Exception e) {
			trayIcon = null;
			tray = null;
		}
	}

	public void showMainWindow() {
		MainFrame.getMainFrameInstance().setVisible(true);
	}

	public void setWorstCaseColour(String colour, int health) {
		setAnimatedTrayIcon(HudsonTrayIconHelper.getIcon(Job.convertColour(colour), health));
	}
	
	public void showMessage(String caption, String message, int type) {
		trayIcon.displayMessage(caption, message, type);
	}
	
	public void setToolTip(String tooltip) {
		trayIcon.setToolTip("Hudson Tray Application\n" + tooltip);
	}
	
	public void rebuildPopupMenu(List jobs) {
		JPopupMenu popup = new JPopupMenu();
		if (jobs.size() > 0) {
			for (int i = 0; i < jobs.size(); i++) {
				Job job = (Job) jobs.get(i);
				ActionListener jobPopupListener = new ActionListener() {
					private Job jobLink = null;
					public ActionListener prepare(final Job job) {
						jobLink = job;
						return this;
					}
					public void actionPerformed(ActionEvent e) {
						new Thread(new Runnable() {
							public void run() {
								try{
									try {
										Thread.sleep(500);
									} catch (InterruptedException e){}
									Desktop.browse(new URL(jobLink.getRFC2396CompliantURL()));
								} catch (DesktopException er) {
									showMessage("Could not launch web page", "Tried to launch:\n" + jobLink.getUrl() + "\n" + er.getLocalizedMessage(), TrayIcon.ERROR_MESSAGE_TYPE);
								} catch (IOException er) {
									showMessage("Could not launch web page", "Tried to launch:\n" + jobLink.getUrl() + "\n" + er.getLocalizedMessage(), TrayIcon.ERROR_MESSAGE_TYPE);
								}
							}
						}, "URL Launching Thread").start();
					}
				}.prepare(job);
				JMenuItem jobMenuItem = new JMenuItem(job.getName());
				jobMenuItem.setIcon(getIconFromHealth(job.getWorstHealthScore()));
				jobMenuItem.addActionListener(jobPopupListener);
				popup.add(jobMenuItem);
			}
			popup.addSeparator();
		}
	    
	    ActionListener exitListener = new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	HudsonTrayApp.getHudsonTrayAppInstance().write();
	            System.exit(0);
	        }
	    };

	    ActionListener openFrameListener = new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		showMainWindow();
	    	}
	    };
	    
	    ActionListener updatedListener = new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		HudsonTrayApp.getHudsonTrayAppInstance().update();
	    	}
	    };

	    JMenuItem openFrameItem = new JMenuItem("Open Application");
	    openFrameItem.addActionListener(openFrameListener);
	    popup.add(openFrameItem);

	    JMenuItem updateItem = new JMenuItem("Fetch Update");
	    updateItem.addActionListener(updatedListener);
	    popup.add(updateItem);

	    JMenuItem defaultItem = new JMenuItem("Exit");
	    defaultItem.addActionListener(exitListener);
	    popup.add(defaultItem);
	    
	    trayIcon.setPopupMenu(popup);
	}
	
	public void setAnimatedTrayIcon(AnimatedTrayIcon icon) {
		if (this.animatedTrayIcon != null) {
			animatedTrayIcon.stop();
		}
		this.animatedTrayIcon = icon;
		icon.start();
	}
	
	private Icon getIconFromHealth(int health) {
		if (health == -1) {
			return null;
		} else if (health >= 0) {
			if (health < 20) {
				return Icons.H0019;
			} else if (health < 40) {
				return Icons.H2039;
			} else if (health < 60) {
				return Icons.H4059;
			} else if (health < 80) {
				return Icons.H6079;
			} else if (health <= 100) {
				return Icons.H80PL;
			}
		}
		return null;
	}
}
