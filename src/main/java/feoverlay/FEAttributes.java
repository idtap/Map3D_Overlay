package feoverlay;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class FEAttributes {
	/// <summary>
	/// 日時組
	/// </summary>
	public String DateTimeValid;

	/// <summary>
	/// 高度
	/// </summary>
	public String AltDepth;

	/// <summary>
	/// 座標
	/// </summary>
	public String Location;

	/// <summary>
	/// 裝備型式
	/// </summary>
	public String Type;

	/// <summary>
	/// 番號或編號
	/// </summary>
	public String Uniquedesignation;

	/// <summary>
	/// 速度
	/// </summary>
	public String Speed;

	/// <summary>
	/// 編制層級/裝備數量
	/// </summary>
	public String Quantity;

	/// <summary>
	/// 指揮部
	/// </summary>
	public String HQ;

	/// <summary>
	/// 方向
	/// </summary>
	public String Direction;

	/// <summary>
	/// 強度
	/// </summary>
	public String Strength;

	/// <summary>
	/// 參謀註記
	/// </summary>
	public String Comment;

	/// <summary>
	/// 特殊註記
	/// </summary>
	public String MoreInfo;

	/// <summary>
	/// 上級番號
	/// </summary>
	public String Parent;

	/// <summary>
	/// 可信度
	/// </summary>
	public String Rating;

	/// <summary>
	/// 效果
	/// </summary>
	public String Effectiveness;

	/// <summary>
	/// 來源
	/// </summary>
	public String Signature;

	/// <summary>
	/// 敵我識別
	/// </summary>
	public String IFFSIF;
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmm");
	
	/**
	 * 考慮是否顯隱註記
	 */
	private boolean considerVisible = false;
	
	public FEAttributes(boolean considerVisible) {
		this.considerVisible = considerVisible;
		DateTimeValid = ""; 
		AltDepth = "";
		Location = "";
		Type = "";
		Uniquedesignation = "";
		Speed = "";
		Quantity = "";
		HQ = "";
		Direction = "";
		Strength = "";
		Comment = "";
		MoreInfo = "";
		Parent = "";
		Rating = "";
		Effectiveness = "";
		Signature = "";
		IFFSIF = "";
	}


	/** 依據Attributes Map回填 FEAttributes物件 */
	public void setFEAttributes(Map<String, Object> labelSet) {
		if (labelSet.containsKey("datetimevalid"))
			this.DateTimeValid = labelSet.get("datetimevalid").toString();
		if (labelSet.containsKey("z"))
			this.AltDepth = labelSet.get("z").toString();
		if (labelSet.containsKey("y"))
			this.Location = labelSet.get("y").toString();
		if (labelSet.containsKey("type"))
			this.Type = labelSet.get("type").toString();
		if (labelSet.containsKey("uniquedesignation"))
			this.Uniquedesignation = labelSet.get("uniquedesignation").toString();	
		if (labelSet.containsKey("speed"))
			this.Speed = labelSet.get("speed").toString();	
		if (labelSet.containsKey("quantity"))
			this.Quantity =  labelSet.get("quantity").toString();	
		if (labelSet.containsKey("specialheadquarters"))
			this.HQ = labelSet.get("specialheadquarters").toString();
		if (labelSet.containsKey("direction"))
			this.Direction =  labelSet.get("direction").toString();		
		if (labelSet.containsKey("reinforced"))
			this.Strength = labelSet.get("reinforced").toString();		
		if (labelSet.containsKey("staffcomment"))
			this.Comment = labelSet.get("staffcomment").toString();	
		if (labelSet.containsKey("additionalinformation"))
			this.MoreInfo = labelSet.get("additionalinformation").toString();	
		if (labelSet.containsKey("higherformation"))
			this.Parent = labelSet.get("higherformation").toString();	
		if (labelSet.containsKey("credibility"))
			this.Rating = labelSet.get("credibility").toString();	
		if (labelSet.containsKey("combateffectiveness"))
			this.Effectiveness = labelSet.get("combateffectiveness").toString();	
		if (labelSet.containsKey("signatureequipment"))
			this.Signature = labelSet.get("signatureequipment").toString();	
		if (labelSet.containsKey("idmode"))
			this.IFFSIF = labelSet.get("idmode").toString();
		
		//Unvisible
		if (considerVisible) {
			if (labelSet.containsKey("-datetimevalid"))
				this.DateTimeValid = labelSet.get("-datetimevalid").toString();
			if (labelSet.containsKey("-z"))
				this.AltDepth = labelSet.get("-z").toString();
			if (labelSet.containsKey("-y"))
				this.Location = labelSet.get("-y").toString();
			if (labelSet.containsKey("-type"))
				this.Type = labelSet.get("-type").toString();
			if (labelSet.containsKey("-uniquedesignation"))
				this.Uniquedesignation = labelSet.get("-uniquedesignation").toString();	
			if (labelSet.containsKey("-speed"))
				this.Speed = labelSet.get("-speed").toString();	
			if (labelSet.containsKey("-quantity"))
				this.Quantity =  labelSet.get("-quantity").toString();	
			if (labelSet.containsKey("-specialheadquarters"))
				this.HQ = labelSet.get("-specialheadquarters").toString();
			if (labelSet.containsKey("-direction"))
				this.Direction =  labelSet.get("-direction").toString();		
			if (labelSet.containsKey("-reinforced"))
				this.Strength = labelSet.get("-reinforced").toString();		
			if (labelSet.containsKey("-staffcomment"))
				this.Comment = labelSet.get("-staffcomment").toString();	
			if (labelSet.containsKey("-additionalinformation"))
				this.MoreInfo = labelSet.get("-additionalinformation").toString();	
			if (labelSet.containsKey("-higherformation"))
				this.Parent = labelSet.get("-higherformation").toString();	
			if (labelSet.containsKey("-credibility"))
				this.Rating = labelSet.get("-credibility").toString();	
			if (labelSet.containsKey("-combateffectiveness"))
				this.Effectiveness = labelSet.get("-combateffectiveness").toString();	
			if (labelSet.containsKey("-signatureequipment"))
				this.Signature = labelSet.get("-signatureequipment").toString();	
			if (labelSet.containsKey("-idmode"))
				this.IFFSIF = labelSet.get("-idmode").toString();	
		}
	}
	
	public String ToString() {
		String text = "";
		text += "日時組：" + DateTimeValid + "\n";
		text += "深高度：" + AltDepth + "\n";
		text += "座標：" + Location + "\n";
		text += "裝備型式：" + Type + "\n";
		text += "番號或編號：" + Uniquedesignation + "\n";
		text += "速度 (公里/小時)：" + Speed + "\n";
		text += "數量：" + Quantity + "\n";
		text += "指揮部：" + HQ + "\n";
		text += "方向 (度)：" + Direction + "\n";
		text += "強度：" + Strength + "\n";
		text += "參謀註記：" + Comment + "\n";
		text += "特殊註記：" + MoreInfo + "\n";
		text += "上級番號：" + Parent + "\n";
		text += "可信度：" + Rating + "\n";
		text += "效果：" + Effectiveness + "\n";
		text += "來源：" + Signature + "\n";
		text += "敵我識別：" + IFFSIF + "\n";
		return text;
	}

	public static String getMonthForInt(int month) {
	    String[] monthNames = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
	    return monthNames[month];
	}
	
	public static  String getIntSForMonth(String month) {
		 String[] monthNames = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
        // find length of array
        int len = monthNames.length;
        int i = 0;
  
        // traverse in the array
        while (i < len) {
  
            // if the i-th element is t
            // then return the index
            if (monthNames[i].equals(month)) {
            	String ret = "0" + (i+1);
                return ret.substring(ret.length() - 2);
            }
            else {
                i = i + 1;
            }
        }
        return "-1";
	}
	
	public static  String getValidFromDate(Date date) {
		SimpleDateFormat myFmt1=new SimpleDateFormat("ddHHmmss");
		SimpleDateFormat myFmt2=new SimpleDateFormat("yy");

		String datetimeValid = myFmt1.format(date) + "08H" + getMonthForInt(date.getMonth()) + 
				myFmt2.format(date);
		return datetimeValid;
	}
	
	public static  Date getDateFromValid(String dateTimeValid) {
		String strMonth = dateTimeValid.substring(11, 14);
		String MM = getIntSForMonth(strMonth);
		SimpleDateFormat myFmt1=new SimpleDateFormat("ddHHmmssMMyy");
		
		String paseTxt = dateTimeValid.substring(0, 8) + MM + dateTimeValid.substring(dateTimeValid.length() - 2);
		try {
			return myFmt1.parse(paseTxt);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
