import { useCallback, useState } from "react";

export default function useInput(initialValue, isEditor) {
  const [value, setValue] = useState(initialValue);

  // value, change handler
  const bind = {
    value,
    onChange: useCallback(e => {
      const value = isEditor ? e : e.target.value;
      setValue(value);
    }, []),
  };

  // reset
  const reset = useCallback(() => setValue(initialValue), []);

  // return
  return [bind, reset];
}
