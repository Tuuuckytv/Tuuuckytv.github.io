export type Furniture={id:string;kind:string;x:number;z:number;r:number};
export type Decor={wall:string;floor:string;neon:string;objects:Furniture[];art:string[]};
export type Look={skin:string;hair:string;top:string;bottom:string;style:number};
export type Player={id:string;name:string;x:number;z:number;r:number;seat:string;look:Look;at:number};
export type Asset={id:string;kind:string;filename:string;mime:string;size:number;core:string};
export const defaultLook:Look={skin:"#d9a27a",hair:"#3c2924",top:"#a69bdf",bottom:"#38465e",style:0};
export const defaultDecor:Decor={wall:"#46405e",floor:"#856353",neon:"#a9a0ff",art:[],objects:[{id:"main-tv",kind:"tv",x:-2.8,z:-3.7,r:0},{id:"main-console",kind:"console",x:-0.5,z:-3.7,r:0},{id:"main-arcade",kind:"arcade",x:2,z:-3.8,r:0},{id:"main-books",kind:"books",x:4.5,z:-3.8,r:0},{id:"main-sofa",kind:"sofa",x:-2.5,z:1.7,r:180},{id:"coffee-table",kind:"table",x:-2.5,z:0,r:0},{id:"floor-lamp",kind:"lamp",x:-4.8,z:1.5,r:0},{id:"green-plant",kind:"plant",x:5,z:2.8,r:0},{id:"stereo",kind:"speakers",x:4.9,z:-1.3,r:270}]};
export const furnitureNames:Record<string,string>={tv:"Television",console:"Games console",arcade:"Arcade machine",books:"Bookshelf",sofa:"Sofa",plant:"Plant",lamp:"Floor lamp",table:"Coffee table",speakers:"Stereo"};
