package com.cyanspring.common;

import java.util.List;

public interface IRecoveryProcessor<T> {
	List<T> recover();
}
