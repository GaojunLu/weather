package com.jim.weather.bean;

import java.util.ArrayList;

public class WeatherInfo {
	public Basic basic;
	public ArrayList<Daily_forecast> daily_forecast;
	public ArrayList<Hourly_forecast> hourly_forecast;
	public Now now;
	public String status;
	public Suggestion suggestion;
	
	public class Basic{
		public String city;
		public String cnty;
		public String id;
		public String lat;
		public String lon;
		public Update update;
	}
	public class Daily_forecast{
		public Astro astro;
		public Cond cond;
		public String date;
		public String hum;
		public String pcpn;
		public String pop;
		public String pres;
		public Tmp tmp;
		public String vis;
		public Wind wind;
	}
	public class Hourly_forecast{
		public String date;
		public String hum;
		public String pop;
		public String pres;
		public String tmp;
		public Wind wind;
	}
	public class Now{
		public Cond cond;
		/**
		 * 不稳定
		 */
		@Deprecated
		public String fl;
		public String hum;
		public String pcpn;
		/**
		 * 不稳定
		 */
		@Deprecated
		public String pres;
		public String tmp;
		public String vis;
		public Wind wind;
	}
	public class Suggestion{
		public SuggestionDes comf;
		public SuggestionDes cw;
		public SuggestionDes drsg;
		public SuggestionDes flu;
		public SuggestionDes sport;
		public SuggestionDes trav;
		public SuggestionDes uv;
	}
	
	public class Update{
		public String loc;
		public String utc;
	}
	public class Astro{
		public String sr;
		public String ss;
	}
	public class Cond{
		public String code_d;
		public String code_n;
		public String txt_d;
		public String txt_n;
		public String code;
		public String txt;
	}
	public class Tmp{
		public String max;
		public String min;
	}
	public class Wind{
		public String deg;
		public String dir;
		public String sc;
		public String spd;
	}
	public class SuggestionDes{
		public String brf;
		public String txt;
	}
}
