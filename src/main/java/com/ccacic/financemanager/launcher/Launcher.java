package com.ccacic.financemanager.launcher;

import com.ccacic.assetexchangewrapper.alphavantage.api.AlphaVantageReadOnlyExchange;
import com.ccacic.assetexchangewrapper.btx.api.BittrexExchange;
import com.ccacic.assetexchangewrapper.core.Interval;
import com.ccacic.assetexchangewrapper.core.api.ReadOnlyExchange;
import com.ccacic.assetexchangewrapper.currencyconverterapi.api.CurrencyConverterApiReadOnlyExchange;
import com.ccacic.financemanager.controller.account.CashAccountFXFactory;
import com.ccacic.financemanager.controller.account.CheckingAccountFXFactory;
import com.ccacic.financemanager.controller.account.CreditAccountFXFactory;
import com.ccacic.financemanager.controller.account.FXAccountFrameContainer;
import com.ccacic.financemanager.controller.account.SavingsAccountFXFactory;
import com.ccacic.financemanager.controller.account.StockAccountFXFactory;
import com.ccacic.financemanager.controller.entry.CrypCurrEntryFXFactory;
import com.ccacic.financemanager.controller.entry.FXEntryFrameContainer;
import com.ccacic.financemanager.controller.entry.StockEntryFXFactory;
import com.ccacic.financemanager.controller.main.BlockingProgressActivity;
import com.ccacic.financemanager.controller.main.ConfirmationActivity;
import com.ccacic.financemanager.controller.main.MainActivity;
import com.ccacic.financemanager.controller.main.PasswordActivity;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.fileio.Archiver;
import com.ccacic.financemanager.fileio.FileHandler;
import com.ccacic.financemanager.logger.Logger;
import com.ccacic.financemanager.logger.Logger.Severity;
import com.ccacic.financemanager.model.account.AccountFactory;
import com.ccacic.financemanager.model.account.children.CashAccountAssembler;
import com.ccacic.financemanager.model.account.children.CheckingAccountAssembler;
import com.ccacic.financemanager.model.account.children.CreditAccountAssembler;
import com.ccacic.financemanager.model.account.children.CrypCurrAccountAssembler;
import com.ccacic.financemanager.model.account.children.SavingsAccountAssembler;
import com.ccacic.financemanager.model.account.children.StockAccountAssembler;
import com.ccacic.financemanager.model.config.GeneralConfig;
import com.ccacic.financemanager.model.currency.conversion.CurrencyExchangeFactory;
import com.ccacic.financemanager.model.currency.conversion.UpdateRatesThread;
import com.ccacic.financemanager.model.entry.EntryFactory;
import com.ccacic.financemanager.model.entry.children.CrypCurrEntryAssembler;
import com.ccacic.financemanager.model.entry.children.FiatCurrEntryAssembler;
import com.ccacic.financemanager.model.entry.children.StockEntryAssembler;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Runs the program. Will be revamped to load interfaces and model aspects
 * dynamically and modularly
 * @author Cameron Cacic
 *
 */
public class Launcher extends Application {

	/**
	 * Main entry method
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			try {
				Severity severity = Severity.valueOf(args[0]);
				Logger.setVerbosityLevel(severity);
			} catch (IllegalArgumentException e) {
				System.out.println("Bad logging severity: " + args[0]);
			}
		}
		Logger.startLogging();
		Archiver.register();
		PasswordActivity.register();
		ConfirmationActivity.register();
		BlockingProgressActivity.register();
		launch(args);
	}
	
	/**
	 * Exits the program immediately, attempting to halt all running threads
	 * smoothly. Changes to the model are not saved to the file system to
	 * avoid corruption. Terminates the JVM on completion
	 */
	public static void exitImmediately() {
		
		try {
			UpdateRatesThread.stopAllThreads();
			Logger.getInstance().logError("A system kill has been requested, program is shutting down");
			EventManager.haltEventThreads();
			Logger.getLoggingThread().join(5000);
		} catch (Exception e) {
			Logger.getInstance().logException(e);
		} finally {
			Logger.stopLogging();
		}
		
		System.exit(0);
	}
	
	@Override
	public void stop() throws Exception {
		Logger.getInstance().logDebug("Program shutting down gracefully");
		/*FileHandler fileHandler = FileHandler.getInstance();
		fileHandler.writeFiles();*/
		UpdateRatesThread.stopAllThreads();
		Logger.stopLogging();
		EventManager.haltEventThreads();
		super.stop();
		System.exit(0);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		try {
			FileHandler fileHandler = FileHandler.getInstance();
			if (!fileHandler.loadConfig()) {
				Logger.getInstance().logError("Failed to load configuration");
				exitImmediately();
			}
			
			loadModules();
			
			MainActivity mainActivity = new MainActivity(primaryStage);
			mainActivity.open();
			
			GeneralConfig generalConfig = GeneralConfig.getInstance();
			long sleepTime = Long.parseLong(generalConfig.getValue(GeneralConfig.DATA_FETCH_TIME));
			UpdateRatesThread updateThread = new UpdateRatesThread(sleepTime);
			updateThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads all the modules. To be streamlined in future releases
	 */
	protected void loadModules() {
		
		AccountFactory accountFactory = AccountFactory.getInstance();
		accountFactory.addAssembler(new CashAccountAssembler());
		accountFactory.addAssembler(new CheckingAccountAssembler());
		accountFactory.addAssembler(new SavingsAccountAssembler());
		accountFactory.addAssembler(new CrypCurrAccountAssembler());
		accountFactory.addAssembler(new StockAccountAssembler());
		accountFactory.addAssembler(new CreditAccountAssembler());
		
		EntryFactory entryFactory = EntryFactory.getInstance();
		entryFactory.addAssembler(new CrypCurrEntryAssembler());
		entryFactory.addAssembler(new FiatCurrEntryAssembler());
		entryFactory.addAssembler(new StockEntryAssembler());
		
		CurrencyExchangeFactory exchangeFactory = CurrencyExchangeFactory.getInstance();
		ReadOnlyExchange exchange = new BittrexExchange();
		exchangeFactory.addExchange(exchange.getExchangeName(), exchange);
		exchange = new AlphaVantageReadOnlyExchange(Interval.FIFTEEN_MIN);
		exchangeFactory.addExchange(exchange.getExchangeName(), exchange);
		exchange = new CurrencyConverterApiReadOnlyExchange(Interval.SIXTY_MIN);
		exchangeFactory.addExchange(exchange.getExchangeName(), exchange);
		
		FXAccountFrameContainer accountFrameContainer = FXAccountFrameContainer.getInstance();
		accountFrameContainer.addFrameFactory(new CashAccountFXFactory());
		accountFrameContainer.addFrameFactory(new CheckingAccountFXFactory());
		accountFrameContainer.addFrameFactory(new SavingsAccountFXFactory());
		accountFrameContainer.addFrameFactory(new StockAccountFXFactory());
		accountFrameContainer.addFrameFactory(new CreditAccountFXFactory());
		
		FXEntryFrameContainer entryFrameContainer = FXEntryFrameContainer.getInstance();
		entryFrameContainer.addFrameFactory(new CrypCurrEntryFXFactory());
		entryFrameContainer.addFrameFactory(new StockEntryFXFactory());
		
	}
	
}
