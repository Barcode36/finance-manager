package com.ccacic.financemanager.model.account.children;

import java.util.HashSet;
import java.util.Set;

import com.ccacic.financemanager.model.Delta;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.account.AccountAssembler;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.entry.children.FiatCurrEntry;
import com.ccacic.financemanager.model.tag.Tag;

/**
 * Assembles CreditAccounts. Tagged with DEBT and uses the Fiat currency set by default
 * @author Cameron Cacic
 *
 */
public class CreditAccountAssembler extends AccountAssembler<CreditAccount> {
	
	private static final String defDisplayName = "Credit Account";
	private static final String defEntryType = FiatCurrEntry.class.getSimpleName();
	private static final Set<Currency> defCurrencies = FiatCurrEntry.getFiatCurrs();
	private static final Set<Tag> defTags = new HashSet<>();
	
	public static final String CREDIT_LINE = "credit_line";
	public static final String ANNUAL_FEE = "annual_fee";
	public static final String APR = "apr";
	public static final String MIN_PAYMENT = "min_payment";
	
	/**
	 * Creates a new CreditAccountAssembler with the default values
	 */
	public CreditAccountAssembler() {
		this(defDisplayName, defEntryType, defCurrencies, defTags);
		defTags.add(Tag.DEBT);
	}

	/**
	 * Creates a new CreditAccountAssembler with the passed values
	 * @param displayName the name to display for CreditAccount
	 * @param entryType the Entry type allowed for CreditAccount
	 * @param currencies the Currencies supported by CreditAccount
	 * @param tags the Tags associated with CreditAccount
	 */
    private CreditAccountAssembler(String displayName, String entryType, Set<Currency> currencies,
                                   Set<Tag> tags) {
		super(CreditAccount.class.getSimpleName(), displayName, entryType, currencies, tags);
	}

	@Override
	public CreditAccount assembleAccount(ParamMap paramMap) {
		
		CreditAccount creditAccount = new CreditAccount();
		creditAccount.creditLine(paramMap.getAsDouble(CREDIT_LINE))
		.annualFee(paramMap.getAsDouble(ANNUAL_FEE))
		.apr(paramMap.getAsDouble(APR))
		.minimumPayment(paramMap.getAsDouble(MIN_PAYMENT));
		
		return creditAccount;
		
	}

	@Override
	public void modifyAccount(CreditAccount account, ParamMap paramMap, Delta delta) {
		
		if (paramMap.contains(CREDIT_LINE)) {
			delta.addPartialDelta(CREDIT_LINE, account.getCreditLine());
			account.creditLine(paramMap.getAsDouble(CREDIT_LINE));
			delta.addPartialDelta(CREDIT_LINE, account.getCreditLine());
		}
		if (paramMap.contains(ANNUAL_FEE)) {
			delta.addPartialDelta(ANNUAL_FEE, account.getAnnualFee());
			account.annualFee(paramMap.getAsDouble(ANNUAL_FEE));
			delta.addPartialDelta(ANNUAL_FEE, account.getAnnualFee());
		}
		if (paramMap.contains(APR)) {
			delta.addPartialDelta(APR, account.getAPR());
			account.creditLine(paramMap.getAsDouble(APR));
			delta.addPartialDelta(APR, account.getAPR());
		}
		if (paramMap.contains(MIN_PAYMENT)) {
			delta.addPartialDelta(MIN_PAYMENT, account.getMinimumPayment());
			account.creditLine(paramMap.getAsDouble(MIN_PAYMENT));
			delta.addPartialDelta(MIN_PAYMENT, account.getMinimumPayment());
		}
	}

	@Override
	public ParamMap disassembleAccount(CreditAccount account) {
		
		ParamMap paramMap = new ParamMap();
		
		paramMap.put(CREDIT_LINE, account.getCreditLine() + "");
		paramMap.put(ANNUAL_FEE, account.getAnnualFee() + "");
		paramMap.put(APR, account.getAPR() + "");
		paramMap.put(MIN_PAYMENT, account.getMinimumPayment() + "");
		
		return paramMap;
		
	}

}
