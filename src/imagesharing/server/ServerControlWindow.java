/*
 * Copyright (c) 2019 Felipe Michels Fontoura
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 
 * Except as contained in this notice, the name of the above copyright holder
 * shall not be used in advertising or otherwise to promote the sale, use or
 * other dealings in this Software without prior written authorization.
 */

package imagesharing.server;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import imagesharing.source.ImageSource;

public class ServerControlWindow extends JFrame
{
	private ScreenSharingServerUI ui;

	private JComboBox comboScreen;
	private JComboBox comboDesktopName;

	public ServerControlWindow()
	{
		setTitle("Image Sharing");
		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setContentPane(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JPanel panelScreen = new JPanel();
		GridBagConstraints gbc_panelScreen = new GridBagConstraints();
		gbc_panelScreen.insets = new Insets(0, 0, 5, 0);
		gbc_panelScreen.fill = GridBagConstraints.BOTH;
		gbc_panelScreen.gridx = 0;
		gbc_panelScreen.gridy = 0;
		panel.add(panelScreen, gbc_panelScreen);
		GridBagLayout gbl_panelScreen = new GridBagLayout();
		gbl_panelScreen.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelScreen.rowHeights = new int[] { 0, 0 };
		gbl_panelScreen.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelScreen.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelScreen.setLayout(gbl_panelScreen);

		JLabel labelScreen = new JLabel("Compartilhar:");
		GridBagConstraints gbc_labelScreen = new GridBagConstraints();
		gbc_labelScreen.insets = new Insets(0, 0, 0, 5);
		gbc_labelScreen.anchor = GridBagConstraints.EAST;
		gbc_labelScreen.gridx = 0;
		gbc_labelScreen.gridy = 0;
		panelScreen.add(labelScreen, gbc_labelScreen);

		this.comboScreen = new JComboBox();
		this.comboScreen.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if (e.getStateChange() == ItemEvent.SELECTED)
				{
					Object item = e.getItem();
					if (item instanceof ScreenComboOption)
					{
						ServerControlWindow.this.onItemSelected((ScreenComboOption) item);
					}
				}
			}
		});
		GridBagConstraints gbc_comboScreen = new GridBagConstraints();
		gbc_comboScreen.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboScreen.gridx = 1;
		gbc_comboScreen.gridy = 0;
		panelScreen.add(this.comboScreen, gbc_comboScreen);

		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 1;
		panel.add(tabbedPane, gbc_tabbedPane);

		JPanel panelDesktop = new JPanel();
		panelDesktop.setBorder(new EmptyBorder(5, 5, 5, 5));
		tabbedPane.addTab("Desktop", null, panelDesktop, null);
		GridBagLayout gbl_panelDesktop = new GridBagLayout();
		gbl_panelDesktop.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelDesktop.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelDesktop.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelDesktop.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelDesktop.setLayout(gbl_panelDesktop);

		JLabel labelDesktopName = new JLabel("Nome da tela");
		GridBagConstraints gbc_labelDesktopName = new GridBagConstraints();
		gbc_labelDesktopName.insets = new Insets(0, 0, 5, 5);
		gbc_labelDesktopName.anchor = GridBagConstraints.EAST;
		gbc_labelDesktopName.gridx = 0;
		gbc_labelDesktopName.gridy = 0;
		panelDesktop.add(labelDesktopName, gbc_labelDesktopName);

		this.comboDesktopName = new JComboBox();
		this.comboDesktopName.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if (e.getStateChange() == ItemEvent.SELECTED)
				{
					Object item = e.getItem();
					if (item instanceof DesktopComboOption)
					{
						ServerControlWindow.this.onItemSelected((DesktopComboOption) item);
					}
				}
			}
		});
		GridBagConstraints gbc_comboDesktopName = new GridBagConstraints();
		gbc_comboDesktopName.insets = new Insets(0, 0, 5, 0);
		gbc_comboDesktopName.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboDesktopName.gridx = 1;
		gbc_comboDesktopName.gridy = 0;
		panelDesktop.add(this.comboDesktopName, gbc_comboDesktopName);

		JButton buttonRefreshDesktopList = new JButton("Atualizar");
		buttonRefreshDesktopList.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ServerControlWindow.this.updateDevices();
			}
		});
		GridBagConstraints gbc_buttonRefreshDesktopList = new GridBagConstraints();
		gbc_buttonRefreshDesktopList.gridwidth = 2;
		gbc_buttonRefreshDesktopList.insets = new Insets(0, 0, 0, 5);
		gbc_buttonRefreshDesktopList.gridx = 0;
		gbc_buttonRefreshDesktopList.gridy = 1;
		panelDesktop.add(buttonRefreshDesktopList, gbc_buttonRefreshDesktopList);

		this.setSize(500, 500);

		this.updateDevices();
	}

	protected void onItemSelected(ScreenComboOption option)
	{
		this.ui.setChosenImageSource(option.getImageSource());
	}

	protected void onItemSelected(DesktopComboOption item)
	{
		this.ui.getDesktopImageSource().setSourceDevice(item.getDevice());
	}

	protected void updateDevices()
	{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gDevs = ge.getScreenDevices();

		ArrayList<DesktopComboOption> desktopComboOptions = new ArrayList<DesktopComboOption>();
		desktopComboOptions.add(new DesktopComboOption("Nenhuma opção selecionada", null));
		for (GraphicsDevice gDev : gDevs)
		{
			DisplayMode mode = gDev.getDisplayMode();

			desktopComboOptions.add(new DesktopComboOption(gDev.getIDstring() + " (" + mode.getWidth() + ", " + mode.getHeight() + ")", gDev));
		}

		this.comboDesktopName.setModel(new DefaultComboBoxModel<DesktopComboOption>(desktopComboOptions.toArray(new DesktopComboOption[desktopComboOptions.size()])));
	}

	public ScreenSharingServerUI getUI()
	{
		return this.ui;
	}

	public void setUI(ScreenSharingServerUI value)
	{
		this.ui = value;
		this.comboScreen.setModel(new DefaultComboBoxModel<ScreenComboOption>(new ScreenComboOption[] {
				//
				new ScreenComboOption("Selecione a opção...", null),
				//
				new ScreenComboOption("Desktop", value.getDesktopImageSource())
				//
		}));
	}

	private static class ScreenComboOption
	{
		private String string;
		private ImageSource source;

		public ScreenComboOption(String string, ImageSource source)
		{
			this.string = string;
			this.source = source;
		}

		public ImageSource getImageSource()
		{
			return this.source;
		}

		@Override
		public String toString()
		{
			return this.string;
		}
	}

	private static class DesktopComboOption
	{
		private String string;
		private GraphicsDevice device;

		public DesktopComboOption(String string, GraphicsDevice device)
		{
			super();
			this.string = string;
			this.device = device;
		}

		public GraphicsDevice getDevice()
		{
			return this.device;
		}

		@Override
		public String toString()
		{
			return this.string;
		}
	}
}
