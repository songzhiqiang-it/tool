package cn.com.rquest.securitization.dealdesign;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.rquest.securitization.common.CommonComponent;
import cn.com.rquest.securitization.process.Processor;
import cn.com.rquest.securitization.process.SubTargetTranche;
import cn.com.rquest.securitization.process.TargetTranche;
import cn.com.rquest.securitization.process.standardInput;
import cn.com.rquest.securitization.ui.SecuritizationUI;
import cn.com.rquest.securitization.ui.UIstandardInput;
import cn.com.rquest.securitization.ui.state.GlobalSession;
import cn.com.rquest.securitization.util.Util;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class CreditEnhancementComponent extends CommonComponent {

	private static final long serialVersionUID = 1L;
	
	private static Logger log = LoggerFactory.getLogger(CreditEnhancementComponent.class);

	private CheckBox ceAccountCheckBox; // 增信账户复选框
	private CheckBox ocCheckBox; // 超额抵押复选框
	private CheckBox triggerCheckBox; // 信用触发机制复选框

	private OptionGroup ceAcctountSelection;// 现金账户单选框
	private OptionGroup ocSelection; // 超额抵押单选框

	private Table ocTable; // 超额抵押表格
	private Table triggerTable; // 触发机制表格

	private TextField cashAcctInitFld; // 现金账户初始百分比
	private TextField reserveAcctInitFld; // 储备金账户初始百分比
	private TextField reserveAcctCapFld; // 储备金账户截止百分比
	private TextField reserveAcctTurnCapFld; // 储备金账户每期最大存入百分比
	private TextField ocAmtFld; // 超额抵押金额
	private TextField ocRatioFld; // 超额抵押率
	private TextField monthFld; // 月
	private TextField cumulativeDefaultRateFld; // 累计违约率

	private HorizontalLayout ceAcctConfigLayout; // 增信账户 ：账户选择和具体配置布局
	private HorizontalLayout cashAcctParamLayout; // 现金账户水平布局
	private GridLayout reserveAcctParamLayout; // 储备金账户参数布局
	private HorizontalLayout ocConfigLayout; // 超额抵押金额、抵押率布局
	private VerticalLayout triggerLayout; // 触发机制布局
	private VerticalLayout ocLayout;   //超额抵押整体布局
    private DealDesignViewX dealDesignViewX; //父页面

	private ComboBox currencyUnitSelection; // 人民币单位

	private GlobalSession sessionGlobal;
	private Processor processor;

	// Member for business logic
	private String ocAmtColName;           // 发行量(万元)/(元)
	private boolean ocAmtErrorFlag = true;
	
	//Listener
	private ValueChangeListener ocAmtFldListener;
	private ValueChangeListener ocRatioFldListener;
	private ValueChangeListener cashAcctInitFldListener;
	private ValueChangeListener reserveAcctInitFldListener;
	private ValueChangeListener reserveAcctCapFldListener;
	private ValueChangeListener reserveAcctTurnCapFldListener;
	private ValueChangeListener cumulativeDefaultRateFldListener;
	

	public CreditEnhancementComponent(DealDesignViewX dealDesignViewX) {
		super();
		this.dealDesignViewX = dealDesignViewX;  //用于设置父页面的资产总额和总发行量
		initLayout();
	}

	/**
	 * 整体初始化布局
	 * 
	 * @return
	 */
	private VerticalLayout initLayout() {

		this.removeAllComponents();

		initContentSessionVariables();

		Panel creditEnhancementPanel = new Panel();
		creditEnhancementPanel.setWidth("100%");
		creditEnhancementPanel.setHeight("580px");
		addComponent(creditEnhancementPanel);

		VerticalLayout mainLayout = new VerticalLayout();
		Panel initLayoutPanel = initLayoutInsidePanel();

		mainLayout.addComponent(initLayoutPanel);
		creditEnhancementPanel.setContent(mainLayout);

		return mainLayout;
	}

	/**
	 * 内部信用增级布局
	 * 
	 * @return
	 */
	private Panel initLayoutInsidePanel() {
		// row 1 - subtitle:内部信用增级方式
		Label subtitle = new Label(UIstandardInput.INTERNAL_CREDIT_ENHANCEMENT);
		subtitle.addStyleName("_creditEnhancement-subtitle");

		// row 1 - 人民币单位：元/万元
		Label currencyUnitLabel = new Label(UIstandardInput.THE_UNIT);
		currencyUnitSelection = Util.initCurrencyUnitComboBox();
		currencyUnitSelection.addStyleName("assetsSelection-set-width-90px");

		Label blankLabel = new Label();
		blankLabel.setWidth("10px");
		HorizontalLayout currencyUnitLayout = new HorizontalLayout(currencyUnitLabel, currencyUnitSelection, blankLabel);
		currencyUnitLayout.addStyleName("common-padding-top-5px");

		// row1 - headerLayout
		HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.addComponent(subtitle);
		headerLayout.addComponent(currencyUnitLayout);
		headerLayout.setComponentAlignment(subtitle, Alignment.MIDDLE_LEFT);
		headerLayout.setComponentAlignment(currencyUnitLayout, Alignment.MIDDLE_RIGHT);

		// row 2 - Content Layout
		HorizontalLayout insideContentLayout = initLayoutInsideContents();
		VerticalLayout insidePanelLayout = new VerticalLayout(headerLayout, insideContentLayout);

		Panel subpanel = new Panel();
		subpanel.setContent(insidePanelLayout);

		return subpanel;
	}

	/**
	 * 三块内容布局
	 * 
	 * @return
	 */
	private HorizontalLayout initLayoutInsideContents() {

		// This layout has 3 columns from left to right
		HorizontalLayout contentLayout = new HorizontalLayout();

		// column 1 - Credit Account setup
		VerticalLayout left1stLayout = initLayoutCeAcctount();
		left1stLayout.setWidth("320px");
		contentLayout.addComponent(left1stLayout);

		// column 2 - Over Collateralization setup
		VerticalLayout left2ndLayout = initLayoutOc();
		left2ndLayout.setWidth("287px");
		contentLayout.addComponent(left2ndLayout);

		// column 3 - Trigger Mechanism setup
		VerticalLayout left3rdPanel = initLayoutTrigger();
		contentLayout.addComponent(left3rdPanel);

		return contentLayout;
	}

	/**
	 * 增信账户整体布局
	 * 
	 * @return
	 */
	public VerticalLayout initLayoutCeAcctount() {

		VerticalLayout ceAcctLayout = new VerticalLayout();
		ceAcctLayout.addStyleName("_creditEnhancement-subpanel");

		// 1st row - Credit Enhancement CheckBox
		ceAccountCheckBox = new CheckBox("增信账户");
		ceAccountCheckBox.setIcon(new ThemeResource("img/card.png"));
		ceAccountCheckBox.addStyleName("_creditEnhancement-ceAccount");
		ceAcctLayout.addComponent(ceAccountCheckBox);

		// cutting line
		Panel linePanel = new Panel();
		linePanel.addStyleName("_creditEnhancement-cuttingLine");
		ceAcctLayout.addComponent(linePanel);

		// 2nd row - Credit Enhancement Configuration
		ceAcctConfigLayout = initLayoutCeAcctConfig();
		ceAcctLayout.addComponent(ceAcctConfigLayout);

		return ceAcctLayout;
	}

	/**
	 * 账户信息具体配置
	 * 
	 * @return
	 */
	private HorizontalLayout initLayoutCeAcctConfig() {

		HorizontalLayout ceAcctConfigLayout = new HorizontalLayout();

		// 1st column - Credit Enhancement Account Selection
		ceAcctountSelection = new OptionGroup();
		ceAcctountSelection.addStyleName("_creditEnhancement-selectAccount");
		ceAcctountSelection.addItem(standardInput.CASHACCOUNT);
		ceAcctountSelection.addItem(standardInput.RESERVEDACCOUNT);
		ceAcctountSelection.setItemCaption(standardInput.CASHACCOUNT, "现金账户");
		ceAcctountSelection.setItemCaption(standardInput.RESERVEDACCOUNT, "储备金账户");

		ceAcctConfigLayout.addComponent(ceAcctountSelection);

		// 2nd column - Credit Enhancement Account Configuration
		VerticalLayout ceAcctParamLayout = initLayoutCeAcctParam();
		ceAcctConfigLayout.addComponent(ceAcctParamLayout);

		return ceAcctConfigLayout;
	}

	/**
	 * 增信账户参数布局
	 * 
	 * @return
	 */
	private VerticalLayout initLayoutCeAcctParam() {

		VerticalLayout ceAcctParamLayout = new VerticalLayout();

		// 1st row - Cash Account Param
		HorizontalLayout cashAcctParamLayout = initLayoutCashAcctParam();
		ceAcctParamLayout.addComponent(cashAcctParamLayout);

		// 2nd row - Reserve Account Param
		GridLayout reserveAcctParamLayout = initLayoutReserveAcctParam();
		ceAcctParamLayout.addComponent(reserveAcctParamLayout);

		return ceAcctParamLayout;
	}

	/**
	 * 现金账户布局
	 * 
	 * @return
	 */
	private HorizontalLayout initLayoutCashAcctParam() {

		cashAcctParamLayout = new HorizontalLayout();
		// 初始百分比
		Label cashAcctStartPercentage = new Label("初始百分比 :");
		cashAcctStartPercentage.setStyleName("_creditEnhancement-cashAccount-init");
		cashAcctParamLayout.addComponent(cashAcctStartPercentage);
		// 初始百分比输入框
		cashAcctInitFld = new TextField();
		cashAcctInitFld.setData(UIstandardInput.START_PERCENTAGE);
		cashAcctInitFld.addStyleName("_creditEnhancement-cashAccountField");
		cashAcctInitFld.setWidth("85px");
		cashAcctParamLayout.addComponent(cashAcctInitFld);
		// %
		Label percentageSignLabel = new Label("%");
		percentageSignLabel.addStyleName("_creditEnhancement-penct");
		cashAcctParamLayout.addComponent(percentageSignLabel);

		return cashAcctParamLayout;
	}

	/**
	 * 储备金账户布局
	 * 
	 * @return
	 */
	private GridLayout initLayoutReserveAcctParam() {

		reserveAcctParamLayout = new GridLayout(3, 3); // 3 columns, 3 rows grid
		reserveAcctParamLayout.setStyleName("_creditEnhancement-reserveAcct-gridlayout");
		// 初始百分比
		Label reserveStartPercentage = new Label("初始百分比:");
		reserveAcctParamLayout.addComponent(reserveStartPercentage, 0, 0);
		// 初始百分比输入框
		reserveAcctInitFld = new TextField();
		reserveAcctInitFld.setData(UIstandardInput.START_PERCENTAGE);
		reserveAcctInitFld.addStyleName("_creditEnhancement-reserveAccount-top");
		reserveAcctParamLayout.addComponent(reserveAcctInitFld, 1, 0);
		// %
		Label reserveStartPercentageLabel = new Label("%");
		reserveStartPercentageLabel.addStyleName("_creditEnhancement-credit-table");
		reserveAcctParamLayout.addComponent(reserveStartPercentageLabel, 2, 0);
		// 截止百分比
		Label reserveEndPercentage = new Label("截止百分比:");
		reserveEndPercentage.addStyleName("_creditEnhancement-defaultPercentage");
		reserveAcctParamLayout.addComponent(reserveEndPercentage, 0, 1);
		// 截止百分比输入框
		reserveAcctCapFld = new TextField();
		reserveAcctCapFld.setData(UIstandardInput.END_PERCENTAGE);
		reserveAcctCapFld.addStyleName("_creditEnhancement-reserveAccount-top");
		reserveAcctParamLayout.addComponent(reserveAcctCapFld, 1, 1);
		// %
		Label reserveEndPercentageLabel = new Label("%");
		reserveEndPercentageLabel.addStyleName("_creditEnhancement-credit-table");
		reserveAcctParamLayout.addComponent(reserveEndPercentageLabel, 2, 1);
		// 每期最大存入百分比
		Label reserveTurnCapPercentage = new Label("每期最大存入百分比:");
		reserveTurnCapPercentage.addStyleName("_creditEnhancement-ecahPercentage");
		reserveAcctParamLayout.addComponent(reserveTurnCapPercentage, 0, 2);
		// 每期最大存入百分比输入框
		reserveAcctTurnCapFld = new TextField();
		reserveAcctTurnCapFld.setData(UIstandardInput.TURN_CAP_PERCENTAGE);
		reserveAcctTurnCapFld.addStyleName("_creditEnhancement-reserveAccount-top");
		reserveAcctParamLayout.addComponent(reserveAcctTurnCapFld, 1, 2);
		// %
		Label reserveTurnCapPercentageLabel = new Label("%");
		reserveTurnCapPercentageLabel.addStyleName("_creditEnhancement-credit-table");
		reserveAcctParamLayout.addComponent(reserveTurnCapPercentageLabel, 2, 2);

		return reserveAcctParamLayout;
	}

	/**
	 * 超额抵押布局
	 * 
	 * @return
	 */
	public VerticalLayout initLayoutOc() {

		ocLayout = new VerticalLayout();
		ocLayout.addStyleName("_creditEnhancement-over-subpanel");

		// 1st row - Over Collateralization checkbox
		ocCheckBox = new CheckBox(UIstandardInput.OC);
		ocCheckBox.setIcon(new ThemeResource("img/money.png"));
		ocCheckBox.addStyleName("_creditEnhancement-overcollateralization");
		ocLayout.addComponent(ocCheckBox);

		Panel linePanel = new Panel();
		linePanel.addStyleName("_creditEnhancement-cuttingLine");
		ocLayout.addComponent(linePanel);

		// 2nd row - Over Collateralization configuration
		ocConfigLayout = initLayoutOcConfig();
		ocLayout.addComponent(ocConfigLayout);

		// 3th row - 表格
		ocTable = initLayoutOCTable();
		ocLayout.addComponent(ocTable);
		ocLayout.setComponentAlignment(ocTable, Alignment.MIDDLE_CENTER);

		return ocLayout;
	}

	/**
	 * 超额抵押金额、比例布局
	 * 
	 * @return
	 */
	private HorizontalLayout initLayoutOcConfig() {

		HorizontalLayout ocConfigLayout = new HorizontalLayout();

		// 1st column - Over Collateralization Selection
		ocSelection = new OptionGroup();
		ocSelection.addItem(standardInput.OC_BY_AMOUNT);
		ocSelection.addItem(standardInput.OC_BY_RATIO);
		ocSelection.setItemCaption(standardInput.OC_BY_AMOUNT, "超额抵押金额:");
		ocSelection.setItemCaption(standardInput.OC_BY_RATIO, "超额抵押率：");
		ocSelection.addStyleName("_creditEnhancement-ceAcctSelection");
		ocConfigLayout.addComponent(ocSelection);

		// 2nd column - Over Collateralization Configuration
		VerticalLayout ocParamLayout = initLayoutOcParam();
		ocConfigLayout.addComponent(ocParamLayout);

		return ocConfigLayout;
	}

	/**
	 * 初始化超额抵押参数布局
	 * 
	 * @return
	 */
	private VerticalLayout initLayoutOcParam() {

		VerticalLayout ocParamLayout = new VerticalLayout();
		ocParamLayout.setStyleName("_creditEnhancement-ocParamLayout");

		// 1st row - OC by Amount Param
		HorizontalLayout byAmountParamLayout = new HorizontalLayout();
		ocParamLayout.addComponent(byAmountParamLayout);
		ocAmtFld = new TextField();
		ocAmtFld.addStyleName("_creditEnhancement-ocAmt");
		ocAmtFld.setData(UIstandardInput.OC_AMOUNT);
		ocAmtFld.setWidth("85px");
		byAmountParamLayout.addComponent(ocAmtFld);

		// 2nd row - OC by Ratio Param
		HorizontalLayout byRatioParamLayout = new HorizontalLayout();
		byRatioParamLayout.addStyleName("_creditEnhancement-reserveAccount-top");
		ocParamLayout.addComponent(byRatioParamLayout);

		ocRatioFld = new TextField();
		ocRatioFld.setData(UIstandardInput.OC_PERCENTAGE);
		ocRatioFld.setWidth("85px");

		byRatioParamLayout.addComponent(ocRatioFld);
		Label percentOCLabel = new Label(UIstandardInput.PERCENT);
		percentOCLabel.addStyleName("_creditEnhancement-oc-percent");
		byRatioParamLayout.addComponent(percentOCLabel);

		return ocParamLayout;
	}

	/**
	 * 超额抵押表格
	 * 
	 * @return
	 */
	private Table initLayoutOCTable() {
		ocTable = new Table();
		ocTable.addStyleName("_creditEnhancement-ocTable");

		ocTable.addContainerProperty(UIstandardInput.TYPE, String.class, null);
		ocTable.setColumnAlignment(UIstandardInput.TYPE, Table.ALIGN_CENTER);

		return ocTable;
	}

	/**
	 * 信用触发机制布局
	 * 
	 * @return
	 */
	public VerticalLayout initLayoutTrigger() {

		triggerLayout = new VerticalLayout();
		triggerLayout.addStyleName("_creditEnhancement-trigger-subpanel");

		// 1st row - Trigger Check Box
		triggerCheckBox = new CheckBox(UIstandardInput.TRIGGER_EVENT);
		triggerCheckBox.setIcon(new ThemeResource("img/warning.png"));
		triggerCheckBox.addStyleName("_creditEnhancement-trigger");
		triggerLayout.addComponent(triggerCheckBox);

		Panel linePanel = new Panel();
		linePanel.addStyleName("_creditEnhancement-cuttingLine");
		triggerLayout.addComponent(linePanel);

		// 加载违约率表格
		this.triggerTable = initLayoutTriggerTable();
		triggerLayout.addComponent(triggerTable);
		triggerLayout.setComponentAlignment(triggerTable, Alignment.MIDDLE_CENTER);

		return triggerLayout;
	}

	/**
	 * 加载违约率表格
	 * 
	 * @return
	 */
	public Table initLayoutTriggerTable() {

		triggerTable = new Table();
		triggerTable.setPageLength(1);
		triggerTable.setSizeUndefined();
		triggerTable.addStyleName("_creditEnhancement-triggerevent");
		triggerTable.addContainerProperty(UIstandardInput.MONTH, TextField.class, null);
		triggerTable.setColumnAlignment(UIstandardInput.MONTH, Table.ALIGN_CENTER);
		triggerTable.addContainerProperty(UIstandardInput.CUMULATIVE_DEFAULT_RATE + UIstandardInput.PERCENT_,
				TextField.class, null);
		triggerTable.setColumnAlignment(UIstandardInput.CUMULATIVE_DEFAULT_RATE + UIstandardInput.PERCENT_,
				Table.ALIGN_CENTER);
		
		// 月
		monthFld = new TextField();
		monthFld.setData(UIstandardInput.MONTH);
		monthFld.setInputPrompt("无数据");
		monthFld.addStyleName("_creditEnhancement-type");

		// 累计违约率
		cumulativeDefaultRateFld = new TextField();
		cumulativeDefaultRateFld.setData(UIstandardInput.CUMULATED_LOSS_RATE);
		cumulativeDefaultRateFld.setInputPrompt("无数据");
		cumulativeDefaultRateFld.addStyleName("_creditEnhancement-type");
		triggerTable.addItem(new Object[] { monthFld, cumulativeDefaultRateFld }, 1);
		
		return triggerTable;
	}

	/**
	 * 初始化内容
	 */
	public void initContent() {
		initContentCurrencyUnit();
		initContentCeAcct();
		initContentTrancheTableHeader();
		initContentOc();
		initContentTrigger();
	}

	/**
	 * 获取session中人民币单位
	 */
	private void initContentSessionVariables() {
		this.sessionGlobal = ((SecuritizationUI) UI.getCurrent()).getGlobalSession();
		this.processor = ((SecuritizationUI) UI.getCurrent()).getProcessor();
	}

	/**
	 * 根据session初始化人民币单位
	 */
	private void initContentCurrencyUnit() {

		ocAmtColName = Util.currencyString(UIstandardInput.CIRCULATION, this.sessionGlobal.getCurrencyUnit());
		currencyUnitSelection.select(sessionGlobal.getCurrencyUnit());

	}

	/**
	 * 
	 * 初始化显示信用增强账户部分
	 */
	public void initContentCeAcct() {

		if (this.processor.getLiabilityPool().getAccountFlag() == standardInput.CE_ACCT_NOT_USED) {
			this.ceAccountCheckBox.setValue(false);
			this.ceAcctConfigLayout.setEnabled(false);
		} else { 
			this.ceAccountCheckBox.setValue(true);
			this.ceAcctConfigLayout.setEnabled(true);
		}
		initContentCeAcctSelection(); 
	}

	/**
	 * 
	 * 初始化增信账户配置部分
	 */
	public void initContentCeAcctSelection() {

		switch (this.processor.getLiabilityPool().getAccountFlag()) {
		case 1: // CE by cash account
			this.ceAcctountSelection.setValue(standardInput.CASHACCOUNT);
			this.cashAcctParamLayout.setVisible(true);
			this.reserveAcctParamLayout.setVisible(false);
			Util.setTextFldValue(cashAcctInitFld, this.processor.getLiabilityPool().getCashAccountSize());
			this.processor.getLiabilityPool().setAccountMinSize(0);
			this.processor.getLiabilityPool().setAccountMaxSize(0);
			this.processor.getLiabilityPool().setMaxIncomeSize(0);
			break;
		case 2: // CE by reserve account
			reserveAcctParamLayout.addStyleName("_creditEnhancement-reserveVerLayout-style");
			this.ceAcctountSelection.setValue(standardInput.RESERVEDACCOUNT);
			this.cashAcctParamLayout.setVisible(false);
			this.reserveAcctParamLayout.setVisible(true);
			Util.setTextFldValue(this.reserveAcctInitFld, this.processor.getLiabilityPool().getAccountMinSize());
			Util.setTextFldValue(this.reserveAcctCapFld, this.processor.getLiabilityPool().getAccountMaxSize());
			Util.setTextFldValue(this.reserveAcctTurnCapFld, this.processor.getLiabilityPool().getMaxIncomeSize());
			this.processor.getLiabilityPool().setCashAccountSize(0);
			break;
		case 0: // CE account not used
			this.cashAcctParamLayout.setVisible(false);
			this.reserveAcctParamLayout.setVisible(false);
			this.ceAcctountSelection.unselect(ceAcctountSelection.getValue());
			break;
		}
	}

	/**
	 * 初始化显示超额抵押部分
	 */
	public void initContentOc() {
		initContentOcType();
		initContentOcSelection(); // continue initialize the content of OC
	}

	/**
	 * 检查超额抵押复选框是否被选中
	 */
	private void initContentOcType() {
		
		if (this.processor.getLiabilityPool().getOCFlag() == standardInput.OC_NOT_USED) {
			this.ocCheckBox.setValue(false);
			this.ocConfigLayout.setEnabled(false);
		} else {

			this.ocCheckBox.setValue(true);
			this.ocConfigLayout.setEnabled(true);
		}
	}

	/**
	 * 初始化超额抵押配置部分
	 */
	public void initContentOcSelection() {
		switch (this.processor.getLiabilityPool().getOCFlag()) {
		case 1: // setup OC by amount
			this.ocSelection.setValue(standardInput.OC_BY_AMOUNT);
			this.ocAmtFld.setEnabled(true);
			this.ocRatioFld.setEnabled(false);
			Util.setAmtValue(this.ocAmtFld, this.processor.getLiabilityPool().getOCAmt(), this.sessionGlobal.getCurrencyUnit());
			Util.setTextFldValue(this.ocRatioFld, this.processor.getLiabilityPool().getOCAmtSize()*100);
			break;
		case 2: // setup OC by ratio
			this.ocSelection.setValue(standardInput.OC_BY_RATIO);
			this.ocAmtFld.setEnabled(false);
			this.ocRatioFld.setEnabled(true);
			Util.setAmtValue(ocAmtFld, this.processor.getLiabilityPool().getOCAmt(),
					this.sessionGlobal.getCurrencyUnit());
			Util.setTextFldValue(this.ocRatioFld, this.processor.getLiabilityPool().getOCAmtSize()*100);
			break;
		case 0: // OC not used
			this.ocAmtFld.setEnabled(false);
			this.ocRatioFld.setEnabled(false);
			this.ocSelection.unselect(ocSelection.getValue());
			this.processor.getLiabilityPool().setOCAmt(0);
			this.processor.getLiabilityPool().setOCAmtSize(0);
			Util.setTextFldValue(this.ocRatioFld, 0);
			Util.setTextFldValue(this.ocAmtFld, 0);
			break;
		}
		
		initContentTrancheTable();
	}

	/**
	 * 加载表格数据
	 */
	public void initContentTrancheTable() {
		
		ocLayout.removeComponent(ocTable);
		ocTable = initLayoutOCTable();
		ocLayout.addComponent(ocTable);
		ocLayout.setComponentAlignment(ocTable, Alignment.MIDDLE_CENTER);
		
		//调用动态生成表头名称
		initContentTrancheTableHeader();
		// 调用后台计算
		this.processor.getLiabilityPool().calculateIssueAmt(); 
		List<TargetTranche> targetTrancheList = this.processor.getRunConfig().getTargetTrancheList();
		
		//内容从表格的第二行开始
		int i = 2;
		for (TargetTranche t : targetTrancheList) {
			if (t.getSubTargetTrancheList().size() > 0) {
				//如果有子层级，显示子层级类型及发行量
				List<SubTargetTranche> subTargetTrancheList = t.getSubTargetTrancheList();

				for (SubTargetTranche s : subTargetTrancheList) {
					ocTable.addItem(
							new Object[] { s.getName(),
									Util.currencyValue(s.getIssueAmt(), sessionGlobal.getCurrencyUnit()) }, i);
					i++;
				}
			} else {
				//没有子层级，显示父层级类型及发行量
				ocTable.addItem(
						new Object[] { t.getName(),
								Util.currencyValue(t.getIssueAmt(), sessionGlobal.getCurrencyUnit()) }, i);
				i++;
			}
		}
		ocTable.setPageLength(5);
	}
	
	/**
	 * 动态加载发行量表头
	 */
	private void initContentTrancheTableHeader() {
		this.ocAmtColName = Util.currencyString(UIstandardInput.CIRCULATION, this.sessionGlobal.getCurrencyUnit());
		ocTable.addContainerProperty(this.ocAmtColName, Double.class, null);
		ocTable.setColumnAlignment(this.ocAmtColName, Table.ALIGN_CENTER);
	}

	/**
	 * 初始化显示信用触发机制部分
	 */
	public void initContentTrigger() {

		if (this.processor.getLiabilityPool().getTriggerFlag() == standardInput.TRIGGER_NOT_USED) {
			this.triggerCheckBox.setValue(false);
			this.triggerTable.setEnabled(false);
			
		} else { 
			
			this.triggerCheckBox.setValue(true);
			this.triggerTable.setEnabled(true);
		}
		
		Util.setTextFldValue(this.monthFld, this.processor.getLiabilityPool().getMonthNbr());
		Util.setTextFldValue(this.cumulativeDefaultRateFld, this.processor.getLiabilityPool().getTargetCumCLRate());
		
		initContentTriggerTable(); 
	}

	/**
	 * Initialize trigger event list
	 */
	private void initContentTriggerTable() {

	    if((this.processor.getRunConfig().getTargetTrancheList() != null)&&(this.processor.getRunConfig().getTargetTrancheList().size()>0) &&
	            !standardInput.CASHFLOWWATERFALL.equals(this.processor.getRunConfig().getTargetTrancheList().get(0)
	                  .getPrinciplePayType())) {
	    	this.triggerLayout.setVisible(false);
	    }else {
	    	this.triggerLayout.setVisible(true);
	    }	    
	}
	

	public void registerEventHandler() {

		// Currency unit drop down
		currencyUnitSelection.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {

				onSelectCurrencyUnit(event);
			}
		});
		
		ceAccountCheckBox.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				onSelectCeAcct(event);
			}
		});

		ceAcctountSelection.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				onSelectCeAcctType();
			}
		});
		
		registerCashAcctInitFldListener();
		registerReserveAcctInitFldListener();
		registerReserveAcctCapFldListener();
		registerReserveAcctTurnCapFldListener();

//		cashAcctInitFld.addValueChangeListener(new ValueChangeListener() {
//
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public void valueChange(ValueChangeEvent event) {
//				Util.setDoubleRangeValidator(cashAcctInitFld, "范围(0,100)之间", false, false, 0, 100);
//				
//				try {
//					processor.getLiabilityPool().setCashAccountSize(Double.valueOf(cashAcctInitFld.getValue()));
//				} catch (Exception e) {
//					processor.getLiabilityPool().setCashAccountSize(0d);
//				}
//			}
//		});

//		reserveAcctInitFld.addValueChangeListener(new ValueChangeListener() {
//
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public void valueChange(ValueChangeEvent event) {
//				Util.setDoubleRangeValidator(reserveAcctInitFld, "范围[0,100)之间", true, false, 0, 100);
//				
//				try {
//					processor.getLiabilityPool().setAccountMinSize(Double.valueOf(reserveAcctInitFld.getValue()));
//				} catch (Exception e) {
//					processor.getLiabilityPool().setAccountMinSize(0d);
//				}
//			}
//		});
//		reserveAcctCapFld.addValueChangeListener(new ValueChangeListener() {
//
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public void valueChange(ValueChangeEvent event) {
//				Util.setReserveDoubleRangeValidator(reserveAcctInitFld, reserveAcctCapFld);
//				
//				try {
//					processor.getLiabilityPool().setAccountMaxSize(Double.valueOf(reserveAcctCapFld.getValue()));
//				} catch (Exception e) {
//					processor.getLiabilityPool().setAccountMaxSize(0d);
//				}
//				
//			}
//
//		});

//		reserveAcctTurnCapFld.addValueChangeListener(new ValueChangeListener() {
//
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public void valueChange(ValueChangeEvent event) {
//				Util.setDoubleRangeValidator(reserveAcctTurnCapFld, "范围[0,100]之间", true, true, 0, 100);
//				
//				try {
//					processor.getLiabilityPool().setMaxIncomeSize(Double.valueOf(reserveAcctTurnCapFld.getValue()));
//				} catch (Exception e) {
//					processor.getLiabilityPool().setMaxIncomeSize(0d);
//				}
//			}
//		});

		// OC
		ocCheckBox.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				onSelectOc();
			}
		});

		ocSelection.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {

				onSelectOcType();

			}
		});

		registerOcAmtFldListener();
		
		registerOcRatioFldListener();
		
		registerCumulativeDefaultRateFldListener();
		
		// Trigger events
		triggerCheckBox.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {

				onSelectTrigger();

			}
		});
		
//		cumulativeDefaultRateFld.addValueChangeListener(new ValueChangeListener() {
//
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public void valueChange(ValueChangeEvent event) {
//				
//				Util.setDoubleRangeValidator(cumulativeDefaultRateFld, "范围(0,100)之间", false, false, 0, 100);
//				log.info("value is :"+cumulativeDefaultRateFld.getValue());
//				try {
//					processor.getLiabilityPool().setTargetCumCLRate(Double.valueOf(cumulativeDefaultRateFld.getValue()));
//				} catch (Exception e) {
//					processor.getLiabilityPool().setTargetCumCLRate(0d);
//				}
//			}
//		});
		
		monthFld.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;
				@Override
				public void valueChange(ValueChangeEvent event) {
				  
					monthFld.removeAllValidators();
					Validator validator = new RegexpValidator("^[1-9]+\\d*$", "请输入大于0的整数");
					monthFld.addValidator(validator);
					try {
						processor.getLiabilityPool().setMonthNbr(Integer.valueOf(monthFld.getValue()));
					} catch (Exception e) {
						processor.getLiabilityPool().setMonthNbr(0);
					}
					
				}
			});
	}
	
	
	public void registerCashAcctInitFldListener() {
		cashAcctInitFldListener = new ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Util.setDoubleRangeValidator(cashAcctInitFld, "范围(0,100)之间", false, false, 0, 100);
				
				try {
					processor.getLiabilityPool().setCashAccountSize(Double.valueOf(cashAcctInitFld.getValue()));
				} catch (Exception e) {
					processor.getLiabilityPool().setCashAccountSize(0d);
				}
			}
		};
		cashAcctInitFld.addValueChangeListener(cashAcctInitFldListener);
	}
	
	
	public void registerReserveAcctInitFldListener() {
		reserveAcctInitFldListener = new ValueChangeListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Util.setDoubleRangeValidator(reserveAcctInitFld, "范围(0,100)之间", false, false, 0, 100);
				
				try {
					processor.getLiabilityPool().setAccountMinSize(Double.valueOf(reserveAcctInitFld.getValue()));
				} catch (Exception e) {
					processor.getLiabilityPool().setAccountMinSize(0d);
				}
				
			}
		};
		reserveAcctInitFld.addValueChangeListener(reserveAcctInitFldListener);
	}
	
	
	public void registerReserveAcctCapFldListener() {
		reserveAcctCapFldListener = new ValueChangeListener() {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				Util.setReserveDoubleRangeValidator(reserveAcctInitFld, reserveAcctCapFld);
				
				try {
					processor.getLiabilityPool().setAccountMaxSize(Double.valueOf(reserveAcctCapFld.getValue()));
				} catch (Exception e) {
					processor.getLiabilityPool().setAccountMaxSize(0d);
				}
				
			}
		};
		reserveAcctCapFld.addValueChangeListener(reserveAcctCapFldListener);
	}
	
	public void registerReserveAcctTurnCapFldListener() {
		reserveAcctTurnCapFldListener = new ValueChangeListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Util.setDoubleRangeValidator(reserveAcctTurnCapFld, "范围[0,100]之间", true, true, 0, 100);
				
				try {
					processor.getLiabilityPool().setMaxIncomeSize(Double.valueOf(reserveAcctTurnCapFld.getValue()));
				} catch (Exception e) {
					processor.getLiabilityPool().setMaxIncomeSize(0d);
				}
				
			}
		};
		reserveAcctTurnCapFld.addValueChangeListener(reserveAcctTurnCapFldListener);
	}
	
	
	/**
	 * 防止死循环
	 */
	public void registerOcRatioFldListener() {
		ocRatioFldListener = new ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Util.setDoubleRangeValidator(ocRatioFld, "范围(0,100)之间", false, false, 0, 100);

				onUpdateOcRatio(event);

			}
		};
		ocRatioFld.addValueChangeListener(ocRatioFldListener);
	}
	
	public void registerOcAmtFldListener() {
		ocAmtFldListener = new ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {

				// 如果校验通过，再更新发行量(0<输入的数字<资产包总额)
				double totalBalance = processor.getLiabilityPool().getTotalBalance();// 资产包总额
				//此时的人民币单位
				String totalBalanceStr = Util.transferCurrentUnit(totalBalance, sessionGlobal.getCurrencyUnit());
				double tb = Double.parseDouble(totalBalanceStr.replaceAll(",", ""));

				Util.setDoubleRangeValidator(ocAmtFld, "范围(0," + tb + ")之间", false, false, 0, tb);

				onUpdateOcAmt(event);

			}
		};
		
		ocAmtFld.addValueChangeListener(ocAmtFldListener);
	}
	
	public void registerCumulativeDefaultRateFldListener() {
		cumulativeDefaultRateFldListener = new ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Util.setDoubleRangeValidator(cumulativeDefaultRateFld, "范围(0,100)之间", false, false, 0, 100);
				log.info("value is :"+cumulativeDefaultRateFld.getValue());
				try {
					processor.getLiabilityPool().setTargetCumCLRate(Double.valueOf(cumulativeDefaultRateFld.getValue()));
				} catch (Exception e) {
					processor.getLiabilityPool().setTargetCumCLRate(0d);
				}
			}
			
		};
		cumulativeDefaultRateFld.addListener(cumulativeDefaultRateFldListener);
	}
	
	/**
	 * 元、万切换,重新加载页面
	 */
	public void onSelectCurrencyUnit(ValueChangeEvent event) {
		this.sessionGlobal.setCurrencyUnit((UIstandardInput.CurrencyUnit) event.getProperty().getValue());
		//重新设置ocAmt的值
		double ocAmt = this.processor.getLiabilityPool().getOCAmt(); //超额抵押金额
		String ocAmtValStr = Util.transferCurrentUnit(ocAmt, sessionGlobal.getCurrencyUnit());
		double ocAmtVal = Double.parseDouble(ocAmtValStr.replaceAll(",", ""));
		Util.setTextFldValue(this.ocAmtFld, ocAmtVal);

		// 重新加载超额抵押的表格
		this.ocAmtColName = Util.currencyString(UIstandardInput.CIRCULATION, this.sessionGlobal.getCurrencyUnit());
		ocLayout.removeComponent(ocTable);
		ocTable = initLayoutOCTable();
		ocLayout.addComponent(ocTable);
		ocLayout.setComponentAlignment(ocTable, Alignment.MIDDLE_CENTER);
		initContentTrancheTableHeader();
		initContentTrancheTable();
		
		 //重新设置资产总额和总发行量的值
        dealDesignViewX.getIntegrationLayout().removeAllComponents();
        dealDesignViewX.initLayoutIntegration();
        dealDesignViewX.initcontentBalance();

	}

	public void onSelectCeAcct(ValueChangeEvent event) {

		// default to cash account
		if (ceAccountCheckBox.getValue())
			this.processor.getLiabilityPool().setAccountFlag(standardInput.CASHACCOUNT);
		else {
			this.processor.getLiabilityPool().setAccountFlag(standardInput.CE_ACCT_NOT_USED);
			this.processor.getLiabilityPool().setCashAccountSize(0);
			this.processor.getLiabilityPool().setAccountMinSize(0);
			this.processor.getLiabilityPool().setAccountMaxSize(0);
			this.processor.getLiabilityPool().setMaxIncomeSize(0);
		}
		// Refresh section
		this.initContentCeAcct();
	}

	public void onSelectCeAcctType() {
		int acctType;

		if (ceAcctountSelection.getValue() != null) {
			if (String.valueOf(standardInput.CASHACCOUNT).equals(ceAcctountSelection.getValue().toString())) {
				acctType = standardInput.CASHACCOUNT;
			} else if (ceAcctountSelection.getValue().toString().equals(String.valueOf(standardInput.RESERVEDACCOUNT))) {
				acctType = standardInput.RESERVEDACCOUNT;
			} else {
				acctType = standardInput.CE_ACCT_NOT_USED;
			}
			this.processor.getLiabilityPool().setAccountFlag(acctType);
		}
		this.cashAcctInitFld.removeListener(cashAcctInitFldListener);
		this.reserveAcctInitFld.removeListener(reserveAcctInitFldListener);
		this.reserveAcctCapFld.removeListener(reserveAcctCapFldListener);
		this.reserveAcctTurnCapFld.removeListener(reserveAcctTurnCapFldListener);
		// Refresh section
		this.initContentCeAcct();
		registerCashAcctInitFldListener();
		registerReserveAcctInitFldListener();
		registerReserveAcctCapFldListener();
		registerReserveAcctTurnCapFldListener();
	}

	public void onSelectOc() {
		// default to by amount
		if (ocCheckBox.getValue())
			this.processor.getLiabilityPool().setOCFlag(standardInput.OC_BY_AMOUNT);
		else {
			this.processor.getLiabilityPool().setOCFlag(standardInput.OC_NOT_USED);
			this.processor.setOCAmt(0);
		}
		// Refresh section
		this.initContentOc();
	}

	public void onSelectOcType() {

		// extract selection
		int ocType = standardInput.OC_NOT_USED;
		if (ocSelection.getValue() != null) {
			
			if (String.valueOf(standardInput.OC_BY_AMOUNT).equals((ocSelection.getValue()).toString()))
				ocType = standardInput.OC_BY_AMOUNT;
			else if (String.valueOf(standardInput.OC_BY_RATIO).equals((ocSelection.getValue()).toString())) {
				ocAmtErrorFlag = true;//此时给成true，防止ocAmt由对改成错，再改选超额抵押率的情况
				ocType = standardInput.OC_BY_RATIO;
			}
				
		}
		// Set new session value
		this.processor.getLiabilityPool().setOCFlag(ocType);
		
		ocRatioFld.removeListener(ocRatioFldListener);
		ocAmtFld.removeListener(ocAmtFldListener);

		// Refresh section
		this.initContentOc();
	
		registerOcAmtFldListener();
		registerOcRatioFldListener();
	}

	/**
	 * 根据超额抵押金额变化计算发行量
	 */
	public void onUpdateOcAmt(ValueChangeEvent event) {
		double ocAmt = 0;
		if(ocAmtFld.getErrorMessage() == null) {
			if (this.ocAmtFld.getValue() != null ) {
				ocAmt = Util.getAmtValue(this.ocAmtFld, this.sessionGlobal.getCurrencyUnit());
			} 
			float ocRatio= (float) (ocAmt/this.processor.getLiabilityPool().getTotalBalance());
			
			this.processor.getLiabilityPool().setOCAmt(ocAmt);
			this.processor.getLiabilityPool().setOCAmtSize(ocRatio);
			ocRatioFld.removeListener(ocRatioFldListener);
			ocAmtFld.removeListener(ocAmtFldListener);
			initContentOc();
			
			 //重新设置资产总额和总发行量的值
	        dealDesignViewX.getIntegrationLayout().removeAllComponents();
	        dealDesignViewX.initLayoutIntegration();
	        dealDesignViewX.initcontentBalance();

			registerOcRatioFldListener();
			registerOcAmtFldListener();
			ocAmtErrorFlag = true;
		}else {
		    ocAmtErrorFlag = false;
		}

	}

	/**
	 * 根据超额抵押比率变化计算发行量
	 */
	public void onUpdateOcRatio(ValueChangeEvent event) {
		
		float ocRatio = 0;
		if(ocRatioFld.getErrorMessage() == null) {
			if (this.ocRatioFld.getValue() != null ) {
				
				ocRatio= Float.parseFloat(ocRatioFld.getValue().replaceAll(",", ""));  //没有%	
				
			} 
			double totalBalance = this.processor.getLiabilityPool().getTotalBalance();// 资产包总额
			//此时的人民币单位
			String tbStr = Util.transferCurrentUnit(totalBalance, sessionGlobal.getCurrencyUnit());		
			double tb = Double.parseDouble(tbStr.replaceAll(",", ""));
			
			double ocAmt = tb * ocRatio * 0.01;
			
			//把OcAmt转换成元的单位传入后台计算
			ocAmt = Util.getAmtValue(ocAmt, sessionGlobal.getCurrencyUnit());		
			
			this.processor.getLiabilityPool().setOCAmt(ocAmt);
			this.processor.getLiabilityPool().setOCAmtSize(ocRatio * 0.01);
			
			ocRatioFld.removeListener(ocRatioFldListener);
			ocAmtFld.removeListener(ocAmtFldListener);
			initContentOc();
			
			 //重新设置资产总额和总发行量的值
	        dealDesignViewX.getIntegrationLayout().removeAllComponents();
	        dealDesignViewX.initLayoutIntegration();
	        dealDesignViewX.initcontentBalance();

			registerOcRatioFldListener();
			registerOcAmtFldListener();
		}
		
	}

	public void onSelectTrigger() {
			
		if (triggerCheckBox.getValue()) {
			this.processor.getLiabilityPool().setTriggerFlag(standardInput.TRIGGER_USED);
		} else {
			this.processor.getLiabilityPool().setTriggerFlag(standardInput.TRIGGER_NOT_USED);
			this.processor.getLiabilityPool().setMonthNbr(0);
			this.processor.getLiabilityPool().setTargetCumCLRate(0d);
		}
		this.cumulativeDefaultRateFld.removeListener(cumulativeDefaultRateFldListener);
		this.initContentTrigger();
		registerCumulativeDefaultRateFldListener();
	}
	
	@Override
	public boolean validatorTabError() {
	
		double cashAcctInitVal = this.processor.getLiabilityPool().getCashAccountSize();//现金账户初始百分比
		double reserveAcctInitVal = this.processor.getLiabilityPool().getAccountMinSize();//储备金账户初始百分比
		double reserveAcctCapVal = this.processor.getLiabilityPool().getAccountMaxSize();//储备金账户截止百分比
		double reserveAcctTurnCapVal = this.processor.getLiabilityPool().getMaxIncomeSize();//每期最大存入百分比
		
		//增信账户checkBox
		int accountFlag = this.processor.getLiabilityPool().getAccountFlag();
		if(accountFlag != 0) {
			if(cashAcctInitVal == 0 && (reserveAcctInitVal == 0 && reserveAcctCapVal == 0 && reserveAcctTurnCapVal == 0)) {
				return false;
			}
		}
		if(accountFlag == 1) {
			if(cashAcctInitVal <= 0 || cashAcctInitVal >= 100) {
				return false;
			}
		}
		if(accountFlag == 2) {
			if(reserveAcctInitVal < 0 || reserveAcctInitVal >= 100 ) {
				return false;
			}
			if(reserveAcctCapVal <= reserveAcctInitVal || reserveAcctCapVal >= 100) {
				return false;
			}
			if(reserveAcctTurnCapVal < 0 || reserveAcctTurnCapVal > 100) {
				return false;
			}
		}
			
		double ocAmt = this.processor.getLiabilityPool().getOCAmt(); //超额抵押金额
		String ocAmtValStr = Util.transferCurrentUnit(ocAmt, sessionGlobal.getCurrencyUnit());
		double ocAmtVal = Double.parseDouble(ocAmtValStr.replaceAll(",", ""));
		
		double totalBalance = processor.getLiabilityPool().getTotalBalance();// 资产包总额
		String totalBalanceStr = Util.transferCurrentUnit(totalBalance, sessionGlobal.getCurrencyUnit());
		double tbVal = Double.parseDouble(totalBalanceStr.replaceAll(",", ""));
		
		double ocRatioVal = this.processor.getLiabilityPool().getOCAmtSize(); //超额抵押率
		//超额抵押
		int ocFlag = this.processor.getLiabilityPool().getOCFlag();
		if(ocFlag != 0) {
			if(ocAmtVal == 0 && ocRatioVal == 0) {
				return false;
			}
			if(ocAmtVal <= 0 || ocAmtVal >= tbVal) {
	                        return false;
	                  } 
	                  if(ocRatioVal <= 0 || ocRatioVal >=100) {
	                       return false;
	                   }
	                  if(!ocAmtErrorFlag) {
	                      return false;
	                  }
		}
		
		double monthVal = this.processor.getLiabilityPool().getMonthNbr();//月
		double cumulativeDefaultRateVal = this.processor.getLiabilityPool().getTargetCumCLRate();//累计违约率
		
		//信用触发机制
		int triggerFlag = this.processor.getLiabilityPool().getTriggerFlag();
		if(triggerFlag != 0) {
			if(monthVal == 0 || cumulativeDefaultRateVal == 0) {
				return false;
			}
			
			Pattern pattern = Pattern.compile("^[1-9]+\\d*.0$");
			Matcher isNum = pattern.matcher(String.valueOf(monthVal));
			if(!isNum.matches()) {
				return false;
			}
			
			if(cumulativeDefaultRateVal <= 0 || cumulativeDefaultRateVal >= 100) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 默认值为false 代表这个页面中没有被赋值，如果返回值为true，说明已经赋值 
	 */
	public boolean checkIsValue () {
		
		double monthVal = this.processor.getLiabilityPool().getMonthNbr();//月
		double cumulativeDefaultRateVal = this.processor.getLiabilityPool().getTargetCumCLRate();//累计违约率
		//信用触发机制
		int triggerFlag = this.processor.getLiabilityPool().getTriggerFlag();
		if(triggerFlag != 0) {
			if(monthVal != 0 || cumulativeDefaultRateVal != 0) {
				return true;
			}
		}
		return false;
	}
	
	public void resetTriggerTable() {
		this.processor.getLiabilityPool().setTriggerFlag(standardInput.TRIGGER_NOT_USED);
		this.processor.getLiabilityPool().setTargetCumCLRate(0d);
		this.processor.getLiabilityPool().setMonthNbr(0);
	}

	
}
