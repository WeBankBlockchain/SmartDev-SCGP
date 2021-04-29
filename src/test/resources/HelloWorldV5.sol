pragma solidity ^0.5.2;

contract HelloWorldV5{
    string name;


    function set(string memory n) public{
		emit test(n);
    	name = n;
    }

	event test(string a);

    constructor () public{
       name = "Hello, World!";
    }

    function get() public view returns(string memory) {
        return name;
    }

}